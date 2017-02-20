package io.bootique.meta.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Descriptor of a configuration object.
 *
 * @since 0.21
 */
public class ConfigObjectMetadata extends ConfigValueMetadata {

    private static final ConfigMetadataVisitor<Stream<ConfigMetadataNode>> SUB_CONFIGS_EXTRACTOR =
            new ConfigMetadataVisitor<Stream<ConfigMetadataNode>>() {
                @Override
                public Stream<ConfigMetadataNode> visitObjectMetadata(ConfigObjectMetadata metadata) {
                    return metadata.getAllSubConfigs();
                }

                @Override
                public Stream<ConfigMetadataNode> visitValueMetadata(ConfigValueMetadata metadata) {
                    return Stream.empty();
                }

                @Override
                public Stream<ConfigMetadataNode> visitListMetadata(ConfigListMetadata metadata) {
                    return Stream.empty();
                }

                @Override
                public Stream<ConfigMetadataNode> visitMapMetadata(ConfigMapMetadata metadata) {
                    return Stream.empty();
                }
            };

    private boolean abstractType;
    private String typeLabel;

    // can't use ConfigObjectMetadata as subconfig type, as it may also be a proxy
    private Collection<ConfigMetadataNode> subConfigs;
    private Collection<ConfigMetadataNode> properties;

    public ConfigObjectMetadata() {
        this.properties = new ArrayList<>();
        this.subConfigs = new ArrayList<>();
    }

    /**
     * Returns a builder that starts with an already available object. Occasionally it is useful for the caller to
     * provide the initial object, e.g. to cache the instance before the builder is finished to avoid compilation cycles.
     *
     * @param baseObject an initial object that will be further customized by the builder.
     * @return a builder that starts with the provided base object instead of creating a new one.
     */
    public static Builder builder(ConfigObjectMetadata baseObject) {
        return new Builder(baseObject);
    }

    public static Builder builder() {
        return builder(new ConfigObjectMetadata());
    }

    public static Builder builder(String name) {
        return builder().name(name);
    }

    @Override
    public <T> T accept(ConfigMetadataVisitor<T> visitor) {
        return visitor.visitObjectMetadata(this);
    }

    /**
     * Returns an optional label that is used as a type designator for polymorphic config objects.
     *
     * @return an optional label that is used as a type designator for polymorphic config objects.
     */
    public String getTypeLabel() {
        return typeLabel;
    }

    public boolean isAbstractType() {
        return abstractType;
    }

    public Collection<ConfigMetadataNode> getProperties() {
        return properties;
    }

    /**
     * Returns subconfigs that directly inherit from this config.
     *
     * @return subconfigs that directly inherit from this config.
     */
    public Collection<ConfigMetadataNode> getSubConfigs() {
        return subConfigs;
    }

    /**
     * Returns this config plus all subconfigs that directly or indirectly inherit from this config.
     *
     * @return this config plus all subconfigs that directly or indirectly inherit from this config.
     */
    public Stream<ConfigMetadataNode> getAllSubConfigs() {

        if (subConfigs.isEmpty()) {
            return Stream.of(this);
        }

        Stream<ConfigMetadataNode> subconfigs = subConfigs.stream().flatMap(n -> n.accept(SUB_CONFIGS_EXTRACTOR));
        return Stream.concat(Stream.of(this), subconfigs);
    }

    public static class Builder extends ConfigValueMetadata.Builder<ConfigObjectMetadata, Builder> {

        public Builder(ConfigObjectMetadata toBuild) {
            super(toBuild);
        }

        public Builder addProperty(ConfigMetadataNode property) {
            toBuild.properties.add(property);
            return this;
        }

        public Builder addProperties(Collection<? extends ConfigValueMetadata> properties) {
            toBuild.properties.addAll(properties);
            return this;
        }

        public Builder addSubConfig(ConfigMetadataNode subConfig) {
            toBuild.subConfigs.add(subConfig);
            return this;
        }

        public Builder typeLabel(String label) {
            toBuild.typeLabel = label;
            return this;
        }

        public Builder abstractType(boolean isAbstract) {
            toBuild.abstractType = isAbstract;
            return this;
        }
    }
}
