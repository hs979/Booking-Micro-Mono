package buildingblocks.testbase;

import buildingblocks.mediator.abstractions.IMediator;
import buildingblocks.mediator.abstractions.commands.ICommand;
import buildingblocks.mediator.abstractions.queries.IQuery;
import buildingblocks.mediator.abstractions.requests.IRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class TestFixture {

    private final IMediator mediator;
    private final JdbcTemplate jdbcTemplate;

    public TestFixture(ApplicationContext applicationContext) {
        this.mediator = applicationContext.getBean(IMediator.class);
        this.jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
    }

    @SuppressWarnings("unchecked")
    public <TResponse> TResponse send(IRequest<TResponse> request) {
        if (request instanceof ICommand) {
            return mediator.send((ICommand<TResponse>) request);
        } else if (request instanceof IQuery) {
            return mediator.send((IQuery<TResponse>) request);
        }
        return mediator.send(request);
    }

    protected void cleanupJpa() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'public'",
                String.class
        );

        tables.forEach(table ->
                jdbcTemplate.execute("TRUNCATE TABLE " + table + " RESTART IDENTITY CASCADE")
        );
    }
}
