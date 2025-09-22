package pozhidaev.client;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("=== Клиент генерации ключей ===");
            
            if (args.length < 1) {
                System.out.println("Использование: java -jar client.jar <имя_субъекта> [server_url]");
                System.out.println("Пример: java -jar client.jar \"John Doe\" http://localhost:8080");
                System.exit(1);
            }
            
            String subjectName = args[0];
            String serverUrl = args.length > 1 ? args[1] : "http://localhost:8080";
            
            System.out.println("Запрос сертификата для: " + subjectName);
            System.out.println("Сервер: " + serverUrl);
            
            System.out.println("TODO: Реализовать HTTP клиент для запроса сертификата");
            System.out.println("Клиент завершил работу.");
            
        } catch (Exception e) {
            System.err.println("Ошибка клиента: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
