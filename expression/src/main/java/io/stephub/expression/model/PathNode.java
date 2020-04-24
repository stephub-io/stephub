package io.stephub.expression.model;

import io.stephub.expression.EvaluationContext;
import io.stephub.expression.EvaluationException;
import io.stephub.json.*;
import io.stephub.json.Json.JsonType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class PathNode extends JsonValueNode<Json> {
    private final String id;
    private final List<PathIndexNode> indexes;
    private final PathNode subPath;

    @Override
    public Json evaluate(final EvaluationContext ec) {
        final List<String> pathTrace = new ArrayList<>();
        pathTrace.add(this.id);
        Json projection = ec.get(this.id);
        if (projection == null) {
            projection = JsonNull.INSTANCE;
        }
        projection = this.evaluateIndexes(ec, projection, pathTrace);
        if (this.subPath == null) {
            return projection;
        } else {
            return evaluateInSubPath(ec, projection, this.subPath, pathTrace);
        }
    }

    private Json evaluateIndexes(final EvaluationContext ec, Json projection, final List<String> pathTrace) {
        for (final PathIndexNode i : this.indexes) {
            pathTrace.add("[" + i.getValueText() + "]");
            if (projection instanceof JsonObject) {
                final Json ei = i.evaluate(ec);
                if (ei instanceof JsonString) {
                    final String eis = ((JsonString) ei).getValue();
                    projection = ((JsonObject) projection).getOpt(eis);
                } else {
                    throw this.createInvalidIndexException(JsonType.valueOf(ei).toString(), projection, pathTrace);
                }
            } else if (projection instanceof JsonArray) {
                final Json ei = i.evaluate(ec);
                if (ei instanceof JsonNumber) {
                    final Number ein = ((JsonNumber) ei).getValue();
                    if (ein instanceof Integer || ein instanceof Long) {
                        projection = ((JsonArray) projection).getOpt(ein.intValue());
                    } else {
                        throw this.createInvalidIndexException(ein.getClass().getName().toLowerCase(), projection, pathTrace);
                    }
                } else {
                    throw this.createInvalidIndexException(JsonType.valueOf(ei).toString(), projection, pathTrace);
                }
            } else {
                throw this.createInvalidIndexException(null, projection, pathTrace);
            }
        }
        return projection;
    }

    protected EvaluationException createInvalidIndexException(final String indexType, final Json projection, final List<String> pathTrace) {
        return new EvaluationException("Invalid index " + (indexType != null ? ("'" + indexType + "' ") : "") + "in reference '" + pathTrace.stream().collect(Collectors.joining()) + "' to evaluate in JSON of type '" + JsonType.valueOf(projection) + "'");
    }

    protected static Json evaluateInSubPath(final EvaluationContext ec, final Json context, final PathNode subPath, final List<String> pathTrace) {
        final String subPathId = subPath.id;
        pathTrace.add("." + subPathId);
        if (context instanceof JsonObject) {
            Json projection = ((JsonObject) context).getFields().get(subPathId);
            if (projection == null) {
                projection = JsonNull.INSTANCE;
            }
            projection = subPath.evaluateIndexes(ec, projection, pathTrace);
            if (subPath.subPath == null) {
                return projection;
            }
            return evaluateInSubPath(ec, projection, subPath.subPath, pathTrace);
        } else {
            throw new EvaluationException("Invalid reference '" + pathTrace.stream().collect(Collectors.joining()) + "' in JSON of type '" + JsonType.valueOf(context) + "'");
        }
    }

}
