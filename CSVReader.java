
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Map;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;




import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
public class CSVReader {
    private static final int TIMEOUT = 200; // Milliseconds
    private static final int MAX_PORT = 65535; //65535
    private static final int THREAD_POOL_SIZE = 100;
    private Map<Integer, Boolean> portStatus = new ConcurrentHashMap<>();
    public Map<Integer, String> dataMap = new TreeMap<>();

    public void scanPorts(String host) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        for( int port = 1; port <= MAX_PORT; port++) {
            final int currentPort = port;
            executor.submit(() -> {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(host, currentPort), TIMEOUT);
                    portStatus.put(currentPort, true); // Port is open
                } catch (IOException e) {
                    portStatus.put(currentPort, false); // Port is closed or filtered
                }
            });
        }
        int activeThreadCount = Thread.activeCount();

        // Print the number of active threads
        System.out.println("Number of active threads: " + activeThreadCount);

        executor.shutdown();
        System.out.println("Executor is shut down.");// Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted.
        try {
            executor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }System.out.println("All tasks have finished.");

    }

    public void readCSVFile(String pathToCsv) {
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(pathToCsv))) {

            while ((line = br.readLine()) != null) {
                // Use comma as separator
                String[] columns = line.split(",");

                // Print the content on the console
//                for (String column : columns) {
                try{
                    int key = Integer.parseInt(columns[1]);
                    String value = columns[3];
                    dataMap.put(key, value);
                }catch (Exception e){
                    //don't care
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printDataMap() {
        for (Map.Entry<Integer, String> entry : dataMap.entrySet()) {
            System.out.println(  entry.getKey() + " " + entry.getValue());
        }
    }

    private void printOpenPorts() {

        portStatus.forEach((port,isOpen)->

    {
        if (isOpen) {
            System.out.println("Port " + port + " is open.");
        }
    });
}
public void printOpenPortDescriptions() {
        for (Map.Entry<Integer, Boolean> entry : portStatus.entrySet()) {
            int port = entry.getKey();
            boolean isOpen = entry.getValue();

            if (isOpen) {
                String description = dataMap.get(port);
                if (description != null) {
                    System.out.println(  port  + " " + description);
                }
            }
        }
    }
    public void OpenPortRedis() {
        Jedis jedis = null;
        try {
            jedis = new Jedis("localhost");
            // Write the Map contents to Redis
            for (Map.Entry<Integer, Boolean> entry : portStatus.entrySet()) {
                int port = entry.getKey();
                boolean isOpen = entry.getValue();

                if (isOpen) {
                    String description = dataMap.get(port);
                    if (description != null) {
                        jedis.set(String.valueOf(port), description);
                    }
                }
            }

            // Read and print the Redis database contents
            for (Map.Entry<Integer, Boolean> entry : portStatus.entrySet()) {
                int port = entry.getKey();
                boolean isOpen = entry.getValue();

                if (isOpen) {
                    String value = jedis.get(String.valueOf(port));
                    System.out.println("Port: " + port + " Description: " + value);
                }
            }
        } catch (JedisConnectionException e) {
            System.out.println("Could not connect to Redis: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Issue: " + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();  // Always close the connection
            }
        }
    }
    int activeThreadCount = Thread.activeCount();

    public static void main(String[] args) {
        CSVReader reader = new CSVReader();
        String pathToCsv = "service-names-port-numbers.csv"; // replace with your CSV file path
        String host = "127.0.0.1";

        long startTime = System.nanoTime();
        reader.scanPorts(host);
        long endTime = System.nanoTime(); //End the timer
        long timeElapsed = (endTime - startTime) / 1_000_000;  //nanoseconds to milliseconds
        System.out.println("Execution time in milliseconds: " + timeElapsed);
        reader.readCSVFile(pathToCsv);
        reader.printDataMap();
        reader.printOpenPorts();
        reader.printOpenPortDescriptions();
        reader.OpenPortRedis();

//        reader.printResults();
//        reader.printDataFromCSV();






        }
    }
