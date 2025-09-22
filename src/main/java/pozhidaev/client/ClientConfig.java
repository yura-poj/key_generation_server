package pozhidaev.client;

public class ClientConfig {
    private String subjectName;
    private String serverHost = "localhost";
    private int serverPort = 8080;
    private int delaySeconds = 0;
    private boolean crashBeforeRead = false;
    private String outputDir = ".";

    public void parseCommandLine(String[] args) {
        if (args.length == 0) {
            printHelp();
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--name":
                    if (i + 1 < args.length) {
                        this.subjectName = args[++i];
                    }
                    break;
                case "--host":
                    if (i + 1 < args.length) {
                        this.serverHost = args[++i];
                    }
                    break;
                case "--port":
                    if (i + 1 < args.length) {
                        try {
                            this.serverPort = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid port number: " + args[i]);
                            System.exit(1);
                        }
                    }
                    break;
                case "--delay":
                    if (i + 1 < args.length) {
                        try {
                            this.delaySeconds = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid delay value: " + args[i]);
                            System.exit(1);
                        }
                    }
                    break;
                case "--crash":
                    this.crashBeforeRead = true;
                    break;
                case "--output-dir":
                    if (i + 1 < args.length) {
                        this.outputDir = args[++i];
                    }
                    break;
                case "--help":
                    printHelp();
                    System.exit(0);
                    break;
                default:
                    if (!args[i].startsWith("--") && this.subjectName == null) {
                        this.subjectName = args[i];
                    }
                    break;
            }
        }

        if (this.subjectName == null || this.subjectName.trim().isEmpty()) {
            System.err.println("Subject name is required!");
            printHelp();
            System.exit(1);
        }
    }

    public static void printHelp() {
        System.out.println("Key Generation Client");
        System.out.println("Usage: java -jar client.jar [OPTIONS] <subject_name>");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --name <name>        Subject name for certificate (required)");
        System.out.println("  --host <host>        Server hostname or IP (default: localhost)");
        System.out.println("  --port <port>        Server port (default: 8080)");
        System.out.println("  --delay <seconds>    Delay between request and response reading (default: 0)");
        System.out.println("  --crash              Crash before reading response (simulate client failure)");
        System.out.println("  --output-dir <dir>   Output directory for key files (default: current directory)");
        System.out.println("  --help               Show this help");
        System.out.println();
        System.out.println("Output files:");
        System.out.println("  <name>.key          Private key file");
        System.out.println("  <name>.pub          Public key file");
        System.out.println("  <name>.crt          Certificate file");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar client.jar \"John Doe\"");
        System.out.println("  java -jar client.jar --name \"Alice Smith\" --host 192.168.1.100 --port 9090");
        System.out.println("  java -jar client.jar --name \"Bob\" --delay 5 --output-dir /tmp/keys");
        System.out.println("  java -jar client.jar --name \"Test\" --crash");
    }

    public String getSubjectName() { return subjectName; }
    public String getServerHost() { return serverHost; }
    public int getServerPort() { return serverPort; }
    public int getDelaySeconds() { return delaySeconds; }
    public boolean isCrashBeforeRead() { return crashBeforeRead; }
    public String getOutputDir() { return outputDir; }
}
