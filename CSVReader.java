import com.sun.source.tree.Tree;

import java.io.BufferedReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;


public class CSVReader {
    private static final int TIMEOUT = 200; // Milliseconds
    private static final int MAX_PORT = 65535;
    private static final int THREAD_POOL_SIZE = 4;
    private Map<Integer, Boolean> portStatus = new TreeMap<>();
    public Map<Integer, String> dataMap = new TreeMap<>();
    private void printResults() {
        portStatus.forEach((port, isOpen) -> {
            if (isOpen) {
                System.out.println("Port " + port + " is open.");
            }
        });
    }
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
        while (!executor.isTerminated()) {
            System.out.println("Waiting for all tasks to finish...");
            // Wait until all tasks are finished
        }System.out.println("All tasks have finished.");
     printResults();
    }

    public void readCSVFile(String pathToCsv) {
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(pathToCsv))) {
            while ((line = br.readLine()) != null) {
                // Use comma as separator
                String[] columns = line.split(",");
                if (columns.length >= 4) {
                    int key = Integer.parseInt(columns[1]);
                    String value = columns[3];

                    // Check if the key is not already in the map (to ensure distinct keys)
                    if (!dataMap.containsKey(key)) {
                        dataMap.put(key, value);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void printDataFromCSV(){
    for (Map.Entry<Integer, String> entry : dataMap.entrySet()) {
        System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());

    }}
    private void printOpenPorts() {

        portStatus.forEach((port,isOpen)->

    {
        if (isOpen) {
            System.out.println("Port " + port + " is open.");
        }
    });
}
    int activeThreadCount = Thread.activeCount();

    public static void main(String[] args) {
        CSVReader reader = new CSVReader();
        String pathToCsv = "service-names-port-numbers.csv"; // replace with your CSV file path
        String host = "127.0.0.1";

        reader.scanPorts(host);

        reader.readCSVFile(pathToCsv);



        reader.printResults();
        reader.printDataFromCSV();





        }
    }
