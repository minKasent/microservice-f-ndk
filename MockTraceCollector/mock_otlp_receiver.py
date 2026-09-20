#!/usr/bin/env python3
"""
Mock OTLP Receiver - Captures and displays OpenTelemetry traces
This server receives OTLP traces via gRPC and displays their structure in detail
"""

import grpc
from concurrent import futures
import json
from datetime import datetime
from opentelemetry.proto.collector.trace.v1 import trace_service_pb2
from opentelemetry.proto.collector.trace.v1 import trace_service_pb2_grpc
from opentelemetry.proto.common.v1 import common_pb2


def format_timestamp(nanos):
    """Convert nanosecond timestamp to readable format"""
    if nanos == 0:
        return "N/A"
    seconds = nanos / 1_000_000_000
    return datetime.fromtimestamp(seconds).strftime('%Y-%m-%d %H:%M:%S.%f')


def format_attributes(attributes):
    """Format attributes into a readable dictionary"""
    result = {}
    for attr in attributes:
        key = attr.key
        value = attr.value
        
        # Extract value based on type
        if value.HasField('string_value'):
            result[key] = value.string_value
        elif value.HasField('bool_value'):
            result[key] = value.bool_value
        elif value.HasField('int_value'):
            result[key] = value.int_value
        elif value.HasField('double_value'):
            result[key] = value.double_value
        elif value.HasField('array_value'):
            result[key] = [format_any_value(v) for v in value.array_value.values]
        elif value.HasField('kvlist_value'):
            result[key] = format_attributes(value.kvlist_value.values)
        elif value.HasField('bytes_value'):
            result[key] = f"<bytes: {len(value.bytes_value)} bytes>"
    
    return result


def format_any_value(value):
    """Format AnyValue type"""
    if value.HasField('string_value'):
        return value.string_value
    elif value.HasField('bool_value'):
        return value.bool_value
    elif value.HasField('int_value'):
        return value.int_value
    elif value.HasField('double_value'):
        return value.double_value
    return str(value)


def format_span_kind(kind):
    """Convert span kind enum to readable string"""
    kinds = {
        0: "UNSPECIFIED",
        1: "INTERNAL",
        2: "SERVER",
        3: "CLIENT",
        4: "PRODUCER",
        5: "CONSUMER"
    }
    return kinds.get(kind, f"UNKNOWN({kind})")


def format_status(status):
    """Format span status"""
    codes = {
        0: "UNSET",
        1: "OK",
        2: "ERROR"
    }
    return {
        "code": codes.get(status.code, f"UNKNOWN({status.code})"),
        "message": status.message if status.message else ""
    }


class TraceServiceServicer(trace_service_pb2_grpc.TraceServiceServicer):
    """Implementation of OTLP Trace Service"""
    
    def __init__(self):
        self.trace_count = 0
        self.span_count = 0
    
    def Export(self, request, context):
        """Handle incoming trace export requests"""
        self.trace_count += 1
        
        print("\n" + "="*80)
        print(f"📦 RECEIVED TRACE BATCH #{self.trace_count}")
        print(f"⏰ Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        print("="*80)
        
        # Process each resource span
        for resource_span_idx, resource_span in enumerate(request.resource_spans):
            print(f"\n🔷 Resource Span #{resource_span_idx + 1}")
            print("-" * 80)
            
            # Display resource attributes
            if resource_span.resource:
                resource_attrs = format_attributes(resource_span.resource.attributes)
                print(f"\n📋 Resource Attributes:")
                print(json.dumps(resource_attrs, indent=2, ensure_ascii=False))
            
            # Process scope spans
            for scope_span_idx, scope_span in enumerate(resource_span.scope_spans):
                if scope_span.scope:
                    print(f"\n📚 Scope: {scope_span.scope.name} (version: {scope_span.scope.version})")
                
                # Process individual spans
                for span_idx, span in enumerate(scope_span.spans):
                    self.span_count += 1
                    
                    print(f"\n{'─'*80}")
                    print(f"🔹 SPAN #{span_idx + 1} (Total: #{self.span_count})")
                    print(f"{'─'*80}")
                    
                    # Basic span information
                    print(f"Name:           {span.name}")
                    print(f"Trace ID:       {span.trace_id.hex()}")
                    print(f"Span ID:        {span.span_id.hex()}")
                    print(f"Parent Span ID: {span.parent_span_id.hex() if span.parent_span_id else 'ROOT'}")
                    print(f"Kind:           {format_span_kind(span.kind)}")
                    print(f"Start Time:     {format_timestamp(span.start_time_unix_nano)}")
                    print(f"End Time:       {format_timestamp(span.end_time_unix_nano)}")
                    
                    # Calculate duration
                    if span.start_time_unix_nano and span.end_time_unix_nano:
                        duration_ms = (span.end_time_unix_nano - span.start_time_unix_nano) / 1_000_000
                        print(f"Duration:       {duration_ms:.2f} ms")
                    
                    # Span attributes
                    if span.attributes:
                        span_attrs = format_attributes(span.attributes)
                        print(f"\n📌 Span Attributes:")
                        print(json.dumps(span_attrs, indent=2, ensure_ascii=False))
                    
                    # Events
                    if span.events:
                        print(f"\n📅 Events ({len(span.events)}):")
                        for event_idx, event in enumerate(span.events):
                            print(f"  Event #{event_idx + 1}:")
                            print(f"    Name: {event.name}")
                            print(f"    Time: {format_timestamp(event.time_unix_nano)}")
                            if event.attributes:
                                event_attrs = format_attributes(event.attributes)
                                print(f"    Attributes: {json.dumps(event_attrs, ensure_ascii=False)}")
                    
                    # Links
                    if span.links:
                        print(f"\n🔗 Links ({len(span.links)}):")
                        for link_idx, link in enumerate(span.links):
                            print(f"  Link #{link_idx + 1}:")
                            print(f"    Trace ID: {link.trace_id.hex()}")
                            print(f"    Span ID: {link.span_id.hex()}")
                            if link.attributes:
                                link_attrs = format_attributes(link.attributes)
                                print(f"    Attributes: {json.dumps(link_attrs, ensure_ascii=False)}")
                    
                    # Status
                    if span.status:
                        status = format_status(span.status)
                        if status['code'] != 'UNSET':
                            print(f"\n⚠️  Status: {status['code']}")
                            if status['message']:
                                print(f"   Message: {status['message']}")
        
        print("\n" + "="*80)
        print(f"✅ Batch processed successfully!")
        print(f"📊 Total batches: {self.trace_count} | Total spans: {self.span_count}")
        print("="*80 + "\n")
        
        # Return success response
        return trace_service_pb2.ExportTraceServiceResponse()


def serve(port=4317):
    """Start the mock OTLP receiver server"""
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    
    servicer = TraceServiceServicer()
    trace_service_pb2_grpc.add_TraceServiceServicer_to_server(servicer, server)
    
    server.add_insecure_port(f'[::]:{port}')
    server.start()
    
    print("="*80)
    print("🚀 Mock OTLP Trace Receiver Started")
    print("="*80)
    print(f"📡 Listening on port: {port}")
    print(f"🔌 Protocol: gRPC (OTLP)")
    print(f"📍 Endpoint: http://localhost:{port}")
    print("\n💡 Configure your Java application to send traces here:")
    print(f"   -Dotel.exporter.otlp.endpoint=http://localhost:{port}")
    print("\n⏳ Waiting for traces...")
    print("="*80 + "\n")
    
    try:
        server.wait_for_termination()
    except KeyboardInterrupt:
        print("\n\n🛑 Shutting down server...")
        server.stop(0)
        print("✅ Server stopped")


if __name__ == '__main__':
    import sys
    
    port = 4319
    if len(sys.argv) > 1:
        try:
            port = int(sys.argv[1])
        except ValueError:
            print(f"Invalid port: {sys.argv[1]}, using default 4317")
    
    serve(port)
