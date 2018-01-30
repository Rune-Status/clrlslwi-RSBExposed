package clrlslwi.rsbe.dreambot;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.dreambot.LoginToken;
import org.dreambot.ScriptType;
import org.dreambot.api.methods.world.World;
import org.dreambot.server.net.datatype.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.esotericsoftware.minlog.Log.LEVEL_TRACE;

/**
 * This class and associated classes might not be documented. Please read the kryonet documentation.
 *
 * @author Demmonic
 */
@Log
public class DreamBotKryoNet extends Listener {

    /**
     * The URL of the live KryoNet server.
     */
    private static final String LIVE_SERVER = "cdn.dreambot.org";

    /**
     * The URL of the live KryoNet server.
     */
    private static final String SCRIPT_PATH = "http://cdn.dreambot.org/scripts/%s/%s";

    private Client client;

    private final LinkedBlockingDeque<Object> receiveQueue = new LinkedBlockingDeque<>();

    /**
     * Creates and initializes our {@link Client kryonet client}.
     */
    private void createClient() {
        com.esotericsoftware.minlog.Log.set(LEVEL_TRACE);

        client = new Client();
        client.start();
        setupBindings(client.getKryo());
    }

    /**
     * Enables our binding on the provided {@link Kryo kryo} serialization instance.
     *
     * @param kryo The kryo instance to bind to.
     */
    private void setupBindings(Kryo kryo) {
        kryo.register(LoginData.class);
        kryo.register(LoginResponse.class);
        kryo.register(HashSet.class);
        kryo.register(TabCountData.class);
        kryo.register(TabCountResponse.class);
        kryo.register(Collections.class);
        kryo.register(GetScriptsFreeData.class);
        kryo.register(GetScriptsResponse.class);
        kryo.register(ScriptData.class);
        kryo.register(ArrayList.class);
        kryo.register(GetScriptsPremData.class);
        kryo.register(ScriptMainClassResponse.class);
        kryo.register(ScriptMainClassData.class);
        kryo.register(ScriptClassRequestResponse.class);
        kryo.register(ScriptClassRequestData.class);
        kryo.register(byte[].class);
        kryo.register(GetWorldsData.class);
        kryo.register(GetWorldsResponse.class);
        kryo.register(World.class);
        kryo.register(World[].class);
    }

    /**
     * Disposes the currently running {@link Client kryonet client}.
     */
    private void dispose() {
        try {
            if (client != null && client.isConnected()) {
                client.close();
                client.stop();
                client.dispose();
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to dispose (?)");
        }
    }

    /**
     * Re-creates the currently running {@link Client kryonet client}.
     */
    private void recreateClient() {
        dispose();
        createClient();
    }

    /**
     * Creates a new {@link Client kryonet client} and attempts to connect to a server.
     *
     * @param ip The ip address of the server to connect to.
     * @return If the connection was successful.
     */
    private boolean connect(String ip) {
        recreateClient();

        try {
            client.connect(10_000, ip, 44443);
            return client.isConnected();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to connect!", e);
            return false;
        }
    }

    /**
     * Checks if we're connected to a server.
     *
     * @return If we're connected.
     */
    public boolean isConnected() {
        return client.isConnected();
    }

    /**
     * Attempts to authenticate against a remote server.
     *
     * @param ip       The ip address to authenticate against.
     * @param username The username to authenticate with.
     * @param password The password to authenticate with.
     * @return The {@link LoginToken login info}, or null if login failed.
     */
    public LoginToken tryLogin(String ip, String username, String password) {
        if (!connect(ip)) {
            log.log(Level.WARNING, "Failed to connect!");
            return null;
        }

        client.addListener(this);

        LoginData request = new LoginData();
        request.setUsername(username);
        request.setPassword(password);

        try {
            LoginResponse response = request(request, LoginResponse.class);
            if (response.getMemberID() == -1) {
                log.log(Level.WARNING, "Failed to login!");
                return null;
            }

            log.log(Level.INFO, response.toString());
            return new LoginToken(response.getHash(), response.getMemberID());
        } catch (InterruptedException e) {
            log.log(Level.WARNING, "Logging in was interrupted!", e);
            return null;
        }
    }

    /**
     * Attempts to authenticate against the live server.
     *
     * @param username The username to authenticate with.
     * @param password The password to authenticate with.
     * @return The {@link LoginToken login info}, or null if login failed.
     */
    public LoginToken tryLogin(String username, String password) {
        return tryLogin(LIVE_SERVER, username, password);
    }

    /**
     * Requests the {@link ScriptData scripts} that we're subscribed to on the currently
     * logged in account.
     *
     * @param token The token associated with the current login.
     * @param type  The {@link ScriptType type} of scripts to retrieve.
     * @return The retrieved script descriptors.
     */
    public Collection<ScriptData> requestScripts(LoginToken token, ScriptType type) {
        Object req;
        switch (type) {
            case FREE: {
                GetScriptsFreeData data = new GetScriptsFreeData();
                data.setLoginHash(token.getHash());
                data.setMemberID(token.getMemberId());
                req = data;
                break;
            }
            case PREMIUM: {
                GetScriptsPremData data = new GetScriptsPremData();
                data.setLoginHash(token.getHash());
                data.setMemberID(token.getMemberId());
                req = data;
                break;
            }
            default: {
                throw new RuntimeException("Invalid type " + type);
            }
        }
        GetScriptsResponse resp;
        try {
            resp = request(req, GetScriptsResponse.class);
        } catch (InterruptedException e) {
            log.log(Level.WARNING, "Interrupted", e);
            return null;
        }

        if (resp == null) {
            log.log(Level.WARNING, "Resp is null");
            return null;
        }

        return resp.getScripts();
    }

    /**
     * Retrieves the main class of a {@link ScriptData script}.
     *
     * @param token  The token associated with the current login.
     * @param script The script to retrieve the main class of.
     * @return The response we received.
     */
    public ScriptMainClassResponse getMainClass(LoginToken token, ScriptData script) {
        ScriptMainClassData request = new ScriptMainClassData();
        request.setLoginHash(token.getHash());
        request.setMemberID(token.getMemberId());
        request.setModule(script.getScriptmodule());
        request.setRepository(script.getScriptrepo());

        try {
            return request(request, ScriptMainClassResponse.class);
        } catch (InterruptedException e) {
            log.log(Level.WARNING, "Retrieval was interrupted!", e);
            return null;
        }
    }

    /**
     * Retrieves a resource from the SDN.
     *
     * @param location The base location of the resource.
     * @param resource The resource to retrieve.
     * @return The retrieve resource, or null if retrieval failed.
     */
    public static byte[] getResource(String location, String resource) {
        URL url;
        try {
            url = new URL(String.format(SCRIPT_PATH, location, resource.replace('.', '/') + ".class"));
        } catch (MalformedURLException e) {
            log.log(Level.WARNING, "URL is invalid!", e);
            return null;
        }

        try {
            return IOUtils.toByteArray(url);
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to read!", e);
            return null;
        }
    }

    private <T> T request(Object request, Class<T> responseType) throws InterruptedException {
        client.sendTCP(request);
        return takeReceived(responseType);
    }


    private <T> T takeReceived(Class<T> type) throws InterruptedException {
        long end = System.currentTimeMillis() + 15_000L;
        while (System.currentTimeMillis() < end) {
            Object taken = takeReceived();
            if (taken != null && type.isInstance(taken)) {
                return type.cast(taken);
            }
        }
        return null;
    }

    private Object takeReceived() throws InterruptedException {
        return receiveQueue.poll(1, TimeUnit.SECONDS);
    }

    @Override
    public void received(Connection connection, Object object) {
        log.log(Level.INFO, "Received: " + object);
        receiveQueue.offer(object);
    }

}
