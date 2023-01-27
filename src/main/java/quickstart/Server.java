package quickstart;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.osohq.oso.Expression;
import com.osohq.oso.Predicate;
import com.osohq.oso.Variable;
import io.javalin.Javalin;
import com.osohq.oso.Oso;
import io.javalin.http.Context;
import quickstart.Models.Repository;
import quickstart.Models.User;


public class Server {
    public static void main(String[] args) throws IOException {
        Javalin app = Javalin.create();
        Oso oso = new Oso();
        oso.registerClass(User.class, "User");
        oso.registerClass(Repository.class, "Repository");
        oso.loadFile("src/main/java/quickstart/main.polar");
        app.get("/has_intersection_unbound", ctx -> queryWith(oso, ctx, "has_intersection"));
        app.get("/has_intersection_unbound_expected", ctx -> queryWith(oso, ctx, "has_intersection_expected"));
        app.start(5000);
    }

    private static void queryWith(Oso oso, Context ctx, String predicateName) {
        var predicate = new Predicate(predicateName, List.of(
                new Variable("owned_labels"),
                new Variable("allowed_labels")
        ));
        Map<String, Object> bindings = Map.of(
                "owned_labels", List.of("red", "green", "blue")
        );

        var results = oso.query(predicate, bindings, true).results();
        System.err.println(results);
        ctx.result(results.stream()
                .map(result -> result.entrySet().stream()
                        .map(entry -> String.format("<p>%s -> %s</p>", entry.getKey(), printExpression(entry.getValue())))
                        .collect(Collectors.joining("\n")))
                .collect(Collectors.joining("\n\n"))
        );
    }

    private static String printExpression(Object maybeExpression) {
        if (maybeExpression instanceof Expression) {
            var expression = (Expression) maybeExpression;
            var args = expression.getArgs().stream().map(Server::printExpression).collect(Collectors.joining(", "));
            return String.format("%s (%s)", expression.getOperator(), args);
        }
        return maybeExpression.toString();
    }
}
