package org.mbok.cucumberform.expression.model;

import lombok.AllArgsConstructor;
import org.mbok.cucumberform.expression.EvaluationContext;
import org.mbok.cucumberform.expression.EvaluationException;
import org.mbok.cucumberform.json.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class PathNode extends JsonValueNode<Json> {
    private final String id;
    private final List<PathIndexNode> indexes;
    private final PathNode subPath;

    @Override
    public Json evaluate(final EvaluationContext ec) {
        final List<String> pathTrace = new ArrayList<>();
        pathTrace.add(id);
        Json projection = ec.get(id);
        if (projection == null) {
            projection = new JsonNull();
        }
        projection = evaluateIndexes(ec, projection, pathTrace);
        if (subPath == null) {
            return projection;
        } else {
            return evaluateInSubPath(ec, projection, subPath, pathTrace);
        }
    }

    private Json evaluateIndexes(final EvaluationContext ec, Json projection, final List<String> pathTrace) {
        for (final PathIndexNode i : indexes) {
            pathTrace.add("[" + i.getValueText() + "]");
            if (projection instanceof JsonObject) {
                final Json ei = i.evaluate(ec);
                if (ei instanceof JsonString) {
                    final String eis = ((JsonString) ei).getValue();
                    projection = ((JsonObject) projection).getOpt(eis);
                } else {
                    throw createInvalidIndexException(ei.getType().toString(), projection, pathTrace);
                }
            } else if (projection instanceof JsonArray) {
                final Json ei = i.evaluate(ec);
                if (ei instanceof JsonNumber) {
                    final Number ein = ((JsonNumber) ei).getValue();
                    if (ein instanceof Integer || ein instanceof Long) {
                        projection = ((JsonArray) projection).getOpt(ein.intValue());
                    } else {
                        throw createInvalidIndexException(ein.getClass().getName().toLowerCase(), projection, pathTrace);
                    }
                } else {
                    throw createInvalidIndexException(ei.getType().toString(), projection, pathTrace);
                }
            } else {
                throw createInvalidIndexException(null, projection, pathTrace);
            }
        }
        return projection;
    }

    private static EvaluationException createInvalidIndexException(final String indexType, final Json projection, final List<String> pathTrace) {
        return new EvaluationException("Invalid index " + (indexType != null ? ("'" + indexType + "' ") : "") + "in reference '" + pathTrace.stream().collect(Collectors.joining()) + "' to evaluate in JSON of type '" + projection.getType() + "'");
    }

    protected static Json evaluateInSubPath(final EvaluationContext ec, final Json context, final PathNode subPath, final List<String> pathTrace) {
        final String subPathId = subPath.id;
        pathTrace.add("." + subPathId);
        if (context instanceof JsonObject) {
            Json projection = ((JsonObject) context).getFields().get(subPathId);
            if (projection == null) {
                projection = new JsonNull();
            }
            projection = subPath.evaluateIndexes(ec, projection, pathTrace);
            if (subPath.subPath == null) {
                return projection;
            }
            return evaluateInSubPath(ec, projection, subPath.subPath, pathTrace);
        } else {
            throw new EvaluationException("Invalid reference '" + pathTrace.stream().collect(Collectors.joining()) + "' in JSON of type '" + context.getType() + "'");
        }
    }

}
