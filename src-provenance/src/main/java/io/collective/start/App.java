package io.collective.start;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.collective.articles.ArticleDataGateway;
import io.collective.articles.ArticleRecord;
import io.collective.articles.ArticlesController;
import io.collective.endpoints.EndpointDataGateway;
import io.collective.endpoints.EndpointTask;
import io.collective.endpoints.EndpointWorkFinder;
import io.collective.endpoints.EndpointWorker;
import io.collective.restsupport.BasicApp;
import io.collective.restsupport.NoopController;
import io.collective.restsupport.RestTemplate;
import io.collective.workflow.WorkScheduler;
import io.collective.workflow.Worker;
import org.eclipse.jetty.server.handler.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * The App Class uses the BasicApp Kotlin class and starts an instance of an EndpointWorker, an EndpointScheduler,
 * and an EndpointWorkerFinder to pass an ArticleDataGateway to activate a request to the server to access articles
 * from infoq.com and get the article titles and if they are available.
 * This relies on a collection of ArticleRecord to be added each time an article is found while the requests for tasks
 * remain active.
 *
 * @author matthewjchin
 */
public class App extends BasicApp {

    /*
    Initialize default article data gateway ArticleDataGateway
     */
    private static final ArticleDataGateway articleDataGateway = new ArticleDataGateway(List.of(
            new ArticleRecord(10101, "Programming Languages InfoQ Trends Report - October 2019 4", true),
            new ArticleRecord(10106, "Ryan Kitchens on Learning from Incidents at Netflix, the Role of SRE, and Sociotechnical Systems", true)
    ));

    // Collection of each Worker and EndpointTask of tasks in each iteration
    private ArrayList<Worker<EndpointTask>> workers = new ArrayList<>();

    // Variable used to store each finder when EndpointWorker is called
    private EndpointWorkFinder finder;


    /**
     * Start the app in BasicApp and the scheduler WorkScheduler. A Worker is created in the EndpointWorker class for
     * every task that is added for the server to accept while accessing the infoq.com website and finding out if the
     * content from the articles gateway matches or has to be added into that gateway on the server.
     */
    @Override
    public void start() {
        super.start();

        {
            /*
            * Start the worker by creating an EndpointWorker object worker, using the articleDataGateway of articles
            * in the gateway and a new RestTemplate. The EndpointWorkFinder variable is initialized as a new Finder
            * which uses a new EndpointDataGateway, and the (originally null) workers ArrayList collection of Workers
            * and Tasks on the Endpoint side will keep adding the new workers until all tasks are completed or if
            * the server(s) utilized are shut down.
            *
            * Create a WorkScheduler of EndpointTask which will pass the EndpointWorkFinder and workers collection to a
            * new WorkScheduler object. This WorkScheduler constructor can be found in the WorkScheduler Kotlin class.
            * The work scheduler will be started.
            */
            try {
                EndpointWorker worker = new EndpointWorker(new RestTemplate(), articleDataGateway);
                finder = new EndpointWorkFinder(new EndpointDataGateway());
                workers.add(worker);

                WorkScheduler<EndpointTask> scheduler = new WorkScheduler<>(finder, workers, 300);
                scheduler.start();

            } catch (NullPointerException e) {
                throw new NullPointerException();
            }

        }
    }


    /**
     * Constructs an App Object provided the port number generated prior to starting task requests.
     *
     * @param port the port number to be passed to the BasicApp Kotlin class
     */
    public App(int port) {
        super(port);
    }

    /**
     * A collection method that takes in every Handler of information containing details about articles,
     * objects passed, and the ArticleDataGateway being used.
     *
     * @return a collection of all the handlers
     */
    @NotNull
    @Override
    protected HandlerList handlerList() {
        HandlerList list = new HandlerList();
        list.addHandler(new ArticlesController(new ObjectMapper(), articleDataGateway));
        list.addHandler(new NoopController());
        return list;
    }

    /**
     * Gets a port number in String form, retrieves time zone and environment, and initializes an App with that String
     * port number parsed as an Integer. Then, the App is started.
     *
     * @param args list of arguments
     */
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        String port = System.getenv("PORT") != null ? System.getenv("PORT") : "8881";
        App app = new App(Integer.parseInt(port));
        app.start();
    }
}
