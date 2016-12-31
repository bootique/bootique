package io.bootique.help.config;

import io.bootique.help.ConsoleAppender;
import io.bootique.meta.MetadataNode;
import io.bootique.meta.config.ConfigListMetadata;
import io.bootique.meta.config.ConfigMapMetadata;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.config.ConfigObjectMetadata;
import io.bootique.meta.config.ConfigMetadataVisitor;
import io.bootique.meta.config.ConfigValueMetadata;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @since 0.21
 */
class ConfigSectionGenerator implements ConfigMetadataVisitor<Object> {

    static final int DEFAULT_OFFSET = DefaultConfigHelpGenerator.DEFAULT_OFFSET;

    private ConsoleAppender out;

    public ConfigSectionGenerator(ConsoleAppender out) {
        this.out = Objects.requireNonNull(out);
    }

    @Override
    public Object visitObjectMetadata(ConfigObjectMetadata metadata) {
        printTypeHeader(metadata, false);

        List<ConfigMetadataNode> sortedChildren = metadata.getProperties()
                .stream()
                .sorted(Comparator.comparing(MetadataNode::getName))
                .collect(Collectors.toList());

        if (sortedChildren.isEmpty()) {
            return null;
        }

        ConfigMetadataNode last = sortedChildren.get(sortedChildren.size() - 1);

        ConfigSectionGenerator childGenerator = withOffset(DEFAULT_OFFSET);
        sortedChildren.forEach(p -> {
            p.accept(childGenerator);

            if (p != last) {
                println();
            }
        });

        return null;
    }

    @Override
    public Object visitValueMetadata(ConfigValueMetadata metadata) {
        printTypeHeader(metadata, true);
        return null;
    }

    @Override
    public Object visitListMetadata(ConfigListMetadata metadata) {
        printTypeHeader(metadata, false);

        // TODO: should support multiple element types (from META-INF/services/PolymorphicConfiguration)
        metadata.getElementType().accept(new ConfigSectionListChildGenerator(withOffset(DEFAULT_OFFSET)));

        return null;
    }

    @Override
    public Object visitMapMetadata(ConfigMapMetadata metadata) {
        printTypeHeader(metadata, false);

        // TODO: should support multiple element types (from META-INF/services/PolymorphicConfiguration)
        metadata.getValuesType().accept(
                new ConfigSectionMapChildGenerator(metadata.getKeysType(), withOffset(DEFAULT_OFFSET)));

        return null;
    }

    protected ConfigSectionGenerator withOffset(int offset) {
        return new ConfigSectionGenerator(out.withOffset(offset));
    }

    protected void println(String... phrases) {
        out.println(phrases);
    }

    protected void println() {
        out.println();
    }

    protected void printTypeHeader(ConfigValueMetadata metadata, boolean asValue) {
        Type valueType = metadata.getType();

        if (valueType != null) {
            println("# Type: ", typeLabel(valueType));
        }

        if (metadata.getDescription() != null) {
            println("# ", metadata.getDescription());
        }

        if (asValue) {
            String valueLabel = metadata.getType() != null ? sampleValue(metadata.getType()) : "?";
            println(metadata.getName(), ": ", valueLabel);
        } else {
            println(metadata.getName(), ":");
        }
    }

    protected String sampleValue(Type type) {

        // TODO: allow to provide sample values in metadata, so that we can display something useful

        String typeName = type.getTypeName();

        switch (typeName) {
            case "boolean":
            case "java.lang.Boolean":
                return "<true|false>";
            case "int":
            case "java.lang.Integer":
                return "<int>";
            case "byte":
            case "java.lang.Byte":
                return "<byte>";
            case "double":
            case "java.lang.Double":
                return "<double>";
            case "float":
            case "java.lang.Float":
                return "<float>";
            case "short":
            case "java.lang.Short":
                return "<short>";
            case "long":
            case "java.lang.Long":
                return "<long>";
            case "java.lang.String":
                return "<string>";
            default:
                return "<value>";
        }
    }

    protected String typeLabel(Type type) {

        String typeName = type.getTypeName();

        switch (typeName) {
            case "java.lang.Boolean":
                return "boolean";
            case "java.lang.Integer":
                return "int";
            case "java.lang.Byte":
                return "byte";
            case "java.lang.Double":
                return "double";
            case "java.lang.Float":
                return "float";
            case "java.lang.Short":
                return "short";
            case "java.lang.Long":
                return "long";
            case "java.lang.String":
                return "String";
            default:

                if(type instanceof Class) {
                    Class<?> classType = (Class<?>) type;
                    if(Map.class.isAssignableFrom(classType)) {
                        return "Map";
                    }
                    // TODO: decipher collection type... for now hardcoding List type
                    else if(Collection.class.isAssignableFrom(classType)) {
                        return "List";
                    }
                }

                return typeName;
        }
    }
}
