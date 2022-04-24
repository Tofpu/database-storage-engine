package io.tofpu.databasestorage.resolver.key;

public abstract class StorageKeyResolver<T> {
    private final Class<T> type;

    public StorageKeyResolver(final Class<T> type) {
        this.type = type;
    }

    /**
     * This method is used to serialize the object.
     *
     * @param obj the object to be serialized
     *
     * @return the serialized object
     */
    public abstract String serialize(final Object obj);

//    /**
//     * This method is used to deserialize the object.
//     *
//     * @param serialized the serialized object
//     *
//     * @return the deserialized object
//     */
//    public abstract T deserialize(final String serialized);

    /**
     * @return the key resolver type
     */
    public Class<T> getType() {
        return type;
    }
}
