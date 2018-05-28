package io.bootique.value;

/**
 * @since 0.26
 */
public enum BytesUnit {
    BYTES("Bytes", 1),
    KB("Kilobytes", 1024),
    MB("Megabytes", 1024*1024),
    GB("Gigabytes", 1024*1024*1024);

    private String name;
    private long value;

    BytesUnit(final String name, final long value) {
        this.name = name;
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

}
