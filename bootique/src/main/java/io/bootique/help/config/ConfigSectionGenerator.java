package io.bootique.help.config;

import io.bootique.help.FormattedAppender;
import io.bootique.meta.config.ConfigListMetadata;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.config.ConfigObjectMetadata;
import io.bootique.meta.config.ConfigMetadataVisitor;
import io.bootique.meta.config.ConfigValueMetadata;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @since 0.21
 */
class ConfigSectionGenerator implements ConfigMetadataVisitor<Object> {

    private FormattedAppender out;

    public ConfigSectionGenerator(FormattedAppender out) {
        this.out = Objects.requireNonNull(out);
    }

    @Override
    public Object visitConfigMetadata(ConfigObjectMetadata metadata) {
        printTypeHeader(metadata);
        out.printText(metadata.getName(), ":");

        List<ConfigMetadataNode> sortedChildren = metadata.getProperties()
                .stream()
                .sorted(Comparator.comparing(ConfigValueMetadata::getName))
                .collect(Collectors.toList());

        if (sortedChildren.isEmpty()) {
            return null;
        }

        ConfigMetadataNode last = sortedChildren.get(sortedChildren.size() - 1);

        ConfigSectionGenerator childGenerator = withOffset();
        sortedChildren.forEach(p -> {
            p.accept(childGenerator);

            if (p != last) {
                println();
            }
        });

        return null;
    }

    @Override
    public Object visitConfigPropertyMetadata(ConfigValueMetadata metadata) {

        printTypeHeader(metadata);

        if (metadata.getType() != null) {
            printText(metadata.getName(), ": ", sampleValue(metadata.getType()));
        } else {
            printText(metadata.getName(), ": ?");
        }
        return null;
    }

    @Override
    public Object visitConfigListMetadata(ConfigListMetadata metadata) {

        // TODO: decipher collection type... for now hardcoding List type
        printText("# Type: List");

        if (metadata.getDescription() != null) {
            printText("# ", metadata.getDescription());
        }

        printText(metadata.getName(), ":");

        // TODO: should support multiple element types (from META-INF/services/PolymorphicConfiguration)
        metadata.getElementType().accept(new ConfigSectionListChildGenerator(withOffset()));

        return null;
    }

    protected ConfigSectionGenerator withOffset() {
        return new ConfigSectionGenerator(out.withOffset());
    }

    protected ConfigSectionGenerator withOffset(int offset) {
        return new ConfigSectionGenerator(out.withOffset(offset));
    }

    protected void printText(String... parts) {
        out.printText(parts);
    }

    protected void println() {
        out.println();
    }

    protected void printTypeHeader(ConfigValueMetadata metadata) {
        Class<?> valueType = metadata.getType();

        if (valueType != null) {
            printText("# Type: ", typeLabel(valueType));
        }

        if (metadata.getDescription() != null) {
            printText("# ", metadata.getDescription());
        }
    }

    protected String sampleValue(Class<?> type) {

        // TODO: allow to provide sample values in metadata, so that we can display something useful

        String typeName = type.getTypeName();

        switch (typeName) {
            case "boolean":
            case "java.lang.Boolean":
                return "false";
            case "int":
            case "java.lang.Integer":
                return "100";
            case "byte":
            case "java.lang.Byte":
                return "1";
            case "double":
            case "java.lang.Double":
                return "double";
            case "float":
            case "java.lang.Float":
                return "1.1";
            case "short":
            case "java.lang.Short":
                return "1";
            case "long":
            case "java.lang.Long":
                return "10000000";
            case "java.lang.String":
                return "'string'";
            default:
                return "value";
        }
    }

    protected String typeLabel(Class<?> type) {

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
                return typeName;
        }
    }
}
