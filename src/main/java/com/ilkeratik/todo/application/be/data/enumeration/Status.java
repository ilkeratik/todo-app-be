package com.ilkeratik.todo.application.be.data.enumeration;

public enum Status {
  TO_DO,
  IN_PROGRESS,
  DONE;

  public static Status fromValue(String value) {
    for (Status status : Status.values()) {
      if (status.name().equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown enum value: " + value);
  }
}
