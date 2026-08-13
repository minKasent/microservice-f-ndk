package com.ndk.contentservice.entity;

public enum BlockType {
  // Container blocks
  PAGE,           // Top-level page block (like chapter)
  TOGGLE,         // Collapsible toggle block
  
  // Heading blocks
  HEADING_1,
  HEADING_2,
  HEADING_3,
  
  // Text blocks
  PARAGRAPH,
  QUOTE,
  CALLOUT,        // Highlighted box with icon
  
  // List blocks
  BULLET_LIST,
  NUMBERED_LIST,
  
  // Media blocks
  IMAGE,
  VIDEO,
  CODE,
  
  // Layout blocks
  DIVIDER
}
