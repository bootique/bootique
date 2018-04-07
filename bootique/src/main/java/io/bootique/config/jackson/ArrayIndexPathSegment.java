package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A path segment with remaining path being an array index.
 */
class ArrayIndexPathSegment extends PathSegment {

    protected ArrayIndexPathSegment(JsonNode node, PathSegment parent, String incomingPath, String remainingPath) {
        super(node, parent, incomingPath, remainingPath);

        if (remainingPath != null && remainingPath.length() < 3) {

            if (remainingPath.length() < 3) {
                throw new IllegalArgumentException("The path must start with array index [NNN]. Instead got: " + remainingPath);
            }

            if (remainingPath.charAt(0) != ARRAY_INDEX_START) {
                throw new IllegalArgumentException("The path must start with array index [NNN]. Instead got: " + remainingPath);
            }
        }
    }

    @Override
    protected PathSegment createNext() {
        if (remainingPath == null) {
            return null;
        }

        int len = remainingPath.length();
        if (len == 0) {
            return null;
        }

        // looking for ']' or '].'
        // start at index 1.. The first char is known to be '['
        for (int i = 1; i < len; i++) {
            char c = remainingPath.charAt(i);

            if (c == ArrayIndexPathSegment.ARRAY_INDEX_END) {

                // 1. [NNN]
                if (i == len - 1) {
                    return createChild(remainingPath.substring(0, i + 1), remainingPath.substring(i + 1));
                }
                // 2. [NNN].aaaa (i.e. in the second case the dot must follow closing paren)
                else if (remainingPath.charAt(i + 1) == PathSegment.DOT) {
                    return createChild(remainingPath.substring(0, i + 1), remainingPath.substring(i + 2));
                }
                // TODO: 3. [NNN][MMM]
                // 4. Invalid path
                else {
                    throw new IllegalStateException("Invalid path after array index: " + remainingPath);
                }
            }
        }

        throw new IllegalStateException("No closing array index parenthesis: " + remainingPath);
    }

    @Override
    protected PathSegment createChild(String childIndexWithParenthesis, String remainingPath) {
        JsonNode child = node != null ? node.get(toIndex(childIndexWithParenthesis)) : null;
        return new PathSegment(child, this, childIndexWithParenthesis, remainingPath);
    }

    protected int toIndex(String indexWithParenthesis) {

        if (indexWithParenthesis.length() < 3) {
            throw new IllegalArgumentException("Invalid array index. Must be in format [NNN]. Instead got " + indexWithParenthesis);
        }
        String indexString = indexWithParenthesis.substring(1, indexWithParenthesis.length() - 1);
        int index;
        try {
            index = Integer.parseInt(indexString);
        } catch (NumberFormatException nfex) {
            throw new IllegalArgumentException("Non-int array index. Must be in format [NNN]. Instead got " + indexWithParenthesis);
        }

        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException("Invalid negative array index: " + indexWithParenthesis);
        }

        return index;
    }
}
