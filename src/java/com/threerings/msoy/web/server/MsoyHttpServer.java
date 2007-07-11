//
// $Id$

package com.threerings.msoy.web.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.NCSARequestLog;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.nio.BlockingChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHolder;

import com.threerings.msoy.server.ServerConfig;
import com.threerings.msoy.web.client.DeploymentConfig;

import static com.threerings.msoy.Log.log;

/**
 * Handles HTTP requests made of the Msoy server by the AJAX client and other entities.
 */
public class MsoyHttpServer extends Server
{
    /**
     * Creates and prepares our HTTP server for operation but does not yet start listening on the
     * HTTP port.
     */
    public MsoyHttpServer (File logdir)
        throws IOException
    {
        BlockingChannelConnector conn = new BlockingChannelConnector();
        conn.setPort(ServerConfig.httpPort);
        setConnectors(new Connector[] { conn });

        // jetty initialization is weird
        HandlerCollection handlers = new HandlerCollection();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        RequestLogHandler logger = new RequestLogHandler();
        handlers.setHandlers(new Handler[] { contexts, logger });
        setHandler(handlers);

        // set up logging
        logger.setRequestLog(
            new NCSARequestLog(new File(logdir, "access.log.yyyy_mm_dd").getPath()));

        // wire up our various servlets
        Context context = new Context(contexts, "/", Context.NO_SESSIONS);
        for (int ii = 0; ii < SERVLETS.length; ii++) {
            context.addServlet(new ServletHolder(SERVLETS[ii]), "/msoy/" + SERVLET_NAMES[ii]);
            context.addServlet(new ServletHolder(SERVLETS[ii]), "/" + SERVLET_NAMES[ii]);
        }
        context.addServlet(new ServletHolder(new MediaProxyServlet()),
                           DeploymentConfig.PROXY_PREFIX + "*");

        // wire up serving of static content
        context.setWelcomeFiles(new String[] { "index.html" });
        context.setResourceBase(new File(ServerConfig.serverRoot, "pages").getPath());

        context.addServlet(new ServletHolder(new MsoyDefaultServlet()), "/*");
    }

    /** Handles redirecting to our magic version numbered client for embedding and does other
     * fiddling we want. */
    protected static class MsoyDefaultServlet extends DefaultServlet
    {
        protected void doGet (HttpServletRequest req, HttpServletResponse rsp)
            throws ServletException, IOException {
            // TODO: handle this for more than just world-client.swf?
            if (req.getRequestURI().equals("/clients/world-client.swf")) {
                rsp.setContentLength(0);
                rsp.sendRedirect("/clients/" + DeploymentConfig.version + "/world-client.swf");
            } else {
                super.doGet(req, rsp);
            }
        }
    }

    protected static final String[] SERVLET_NAMES = {
        "usersvc",
        "adminsvc",
        "itemsvc",
        "catalogsvc",
        "profilesvc",
        "membersvc",
        "groupsvc",
        "mailsvc",
        "uploadsvc",
        "gamesvc",
        "swiftlysvc",
        "swiftlyuploadsvc",
    };

    protected static final HttpServlet[] SERVLETS = {
        new WebUserServlet(),
        new AdminServlet(),
        new ItemServlet(),
        new CatalogServlet(),
        new ProfileServlet(),
        new MemberServlet(),
        new GroupServlet(),
        new MailServlet(),
        new UploadServlet(),
        new GameServlet(),
        new SwiftlyServlet(),
        new SwiftlyUploadServlet(),
    };
}
