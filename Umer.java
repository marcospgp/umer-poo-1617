import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ArrayDeque;
import javax.swing.UIManager;
import java.awt.EventQueue;

/**
 * O Umer é um programa que permite efetuar a gerência de viagens entre clientes e motoristas.
 * Esta é a classe principal que gere o programa.
 *
 * @author Marcos Pereira, Sérgio Oliveira, Vítor Castro
 * @version 0.1
 * @since 2017-04-23
 */
public final class Umer {

    private static HashMap<String, Vehicle> vehicles = new HashMap<String, Vehicle>(); // vehicleId   -> Vehicle
    private static HashMap<String, Driver> drivers = new HashMap<String, Driver>();  // driverEmail -> Driver
    private static HashMap<String, Client> clients = new HashMap<String, Client>();  // clientEmail -> Client
    private static ArrayList<Trip> tripHistory = new ArrayList<Trip>();
    private static ArrayList<Trip> tripsUnderway = new ArrayList<Trip>();


    private static User loggedAs = null;

    // Prevenir instanciação desta classe (googlar "java final class private constructor")
    private Umer() {
        throw new AssertionError();
    }

    /**
     * Cria um novo veículo
     *
     * @param x         The initial x position of the vehicle
     * @param y         The initial y position of the vehicle
     * @param type      The vehicle type
     * @param newDriver (optional) The new vehicle's driver. Should be null if not needed.
     */
    public static Vehicle createVehicle(double x, double y, String identifier, String type, boolean hasWaitingList) {

        // TODO - Usar hasWaitingList e usar try catch abaixo no VehicleType.valueOf

        // determinar o tipo de veículo (e se existe esse tipo)
        VehicleType typeKnown = VehicleType.valueOf(type);

        Vehicle newVehicle = new Vehicle(x, y, identifier, typeKnown);

        vehicles.put(identifier, newVehicle);

        return newVehicle;
    }

    /**
     * Cria um novo condutor
     */
    public static Driver registerDriver(String email, String name, String password, String address, String birthdate) {

        Driver newDriver = new Driver(email, name, password, address, birthdate);

        drivers.put(email, newDriver);

        return newDriver;
    }

    /**
     * Cria um novo cliente
     */
    public static Client registerClient(String email, String name, String password, String address, String birthdate, double posX, double posY) {

        Client newClient = new Client(email, name, password, address, birthdate, posX, posY);

        clients.put(email, newClient);

        return newClient;
    }

    /**
     * Ocupa um veículo com um dado condutor
     */
    public static void assignDriverToVehicle(String driverEmail, String vehicleIdentifier) {

        Driver curDriver = drivers.get(driverEmail);

        if (curDriver == null || vehicles.get(vehicleIdentifier) == null) {
            return;
        }

        // Iterar pelos veículos para ver se aquele driver já está em algum. Se estiver, remove-o.

        Set vehicleSet = vehicles.entrySet();
        Iterator<Vehicle> i = vehicleSet.iterator();

        while (i.hasNext()) {

            Map.Entry entry = (Map.Entry) i.next();
            Vehicle curVehicle = (Vehicle) entry.getValue();

            if (curVehicle.getDriver() != null && curVehicle.getDriver().getEmail().equals(driverEmail)) {

                curVehicle.setDriver(null);
            }
        }

        // Associar o condutor ao veículo
        vehicles.get(vehicleIdentifier).setDriver(curDriver);
    }

    /**
     * Tenta fazer login numa conta de utilizador
     *
     * @param email   O email do utilizador
     * @param passord A password do utilizador
     * @return        True se o user for um condutor, false se for um cliente,
     *                null caso o login tenha sido inválido.
     */
    public static Boolean login(String email, String password) {

        if (clients.get(email) != null && clients.get(email).getPassword().equals(password)) {

            loggedAs = clients.get(email);

            return false;
        }

        if (drivers.get(email) != null && drivers.get(email).getPassword().equals(password)) {

            loggedAs = drivers.get(email);

            return true;
        }

        return null;
    }

    /**
     * Tenta fazer logout de um utilizador
     *
     * @return        True se o logout foi bem sucedido, caso contrário é retornado false
     */
    public static boolean logout() {

        // Verificar se está algum utilizador logado
        if (Umer.loggedAs != null) {
            Umer.loggedAs = null;
            return true;
        }

        return false;
    }

    /**
     * Retorna true caso exista o veiculo com aquele ID e esteja disponível.
     * Serve de guarda para quando chamamos certo veiculo, para que nao seja
     * feito o pedido de viagem a um veiculo que nao existe ou esta indisponivel.
     */
    private static boolean isTaxiAvailable(String taxiID) {

        Vehicle taxi = vehicles.get(taxiID);

        if (taxi == null || taxi.getDriver() == null) {
            return false;
        }

        return taxi.getDriver().isAvailable();
    }

    /**
     * Retorna true caso exista algum veiculo disponível.
     * Serve de guarda para quando chamamos qualquer veiculo, para que nao seja
     * feito o pedido de viagem a um veiculo que nao existe ou esta indisponivel.
     */
    private static boolean isSomeTaxiAvailable() {

        Iterator i = vehicles.entrySet().iterator();

        while (i.hasNext()) {

            Map.Entry entry = (Map.Entry) i.next();
            Vehicle curVehicle = (Vehicle) entry.getValue();

            if (curVehicle.getDriver() != null && curVehicle.getDriver().isAvailable()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Retorna o veículo pronto a viajar mais próximo do utilizador
     * logado atualmente, apenas se o utilizador for um cliente
     */
    private static Vehicle getNearestReadyVehicle() {

        if ( !(Umer.loggedAs instanceof Client) ) {
            throw new java.lang.Error("Tried to find nearest vehicle, but current user is not a client.");
        }

        Client client = (Client) Umer.loggedAs;

        return client.getNearestReadyVehicle(vehicles);
    }

    /**
     * Retorna o veículo com o ID específico ao utilizador
     * logado atualmente, apenas se o utilizador for um cliente
     */
    public static Trip startTrip(String taxiID, double destPosX, double destPosY) {

        Trip newTrip = null;
        Point destPos = new Point(destPosX, destPosY);

        if ( !(Umer.loggedAs instanceof Client) ) {
            throw new java.lang.Error("Tried to find a specific vehicle, but current user is not a client.");
        }

        // procura por qualquer taxi caso seja string vazia ou por especifico caso nao seja
        if ( (taxiID.equals("") && isSomeTaxiAvailable()) || (isTaxiAvailable(taxiID)) ) {
            Client a = (Client) Umer.loggedAs;
            newTrip = a.getTrip(vehicles, a.getPosition(), destPos, taxiID);
            // Viagem adicionada ao histórico
            a.tripHistory.add(newTrip);
            // Viagem começa a decorrer
            Umer.tripsUnderway.add(newTrip);
            // Atualizar posição do cliente
            a.setPosition(destPos);
            // Atualizar posição do veículo
            newTrip.getVehicle().setPosition(destPos); // atualizar a posição do veículo

            System.out.println("Vehicle: " + newTrip.getVehicle().getIdentifier());
            System.out.println("Driver: " + newTrip.getDriver().getName());
            System.out.println("Origin: " + "(" + newTrip.getOrigin().getX() + "," + newTrip.getOrigin().getY() + ")");
            System.out.println("Destination: " + "(" + newTrip.getDestination().getX() + "," + newTrip.getDestination().getY() + ")");
            System.out.println("Estimated duration: " + newTrip.getEstimatedDuration());
            System.out.println("Real Duration: " + newTrip.getRealDuration());
            newTrip.setTimeStarted();
            System.out.println("Time Started: " + newTrip.getTimeStarted());
            newTrip.setArrivingTime();
            System.out.println("Arriving time: " + newTrip.getArrivingTime());
            System.out.println("Cost: " + newTrip.getCost());
            System.out.println("Pos atualizada client: " + a.getPosition());
            System.out.println("Pos atualizada driver: " + newTrip.getVehicle().getPosition());
        }

        // nao existe aquele veiculo ou nao há nenhum disponível
        else {
            System.out.println("O veículo " + taxiID + " não existe ou não está disponível.");
            return newTrip;
        }

            return newTrip;
    }

    /**
     * Retorna a lista de viagens do utilizador logado
     * em forma de ArrayList de Strings
     */
    public static String getTripHistory () {

        String trips = "";

        for (int i = 0; i < Umer.loggedAs.tripHistory.size(); i++) {
            trips += Umer.loggedAs.tripHistory.get(i).toString();
            trips += "\n";
        }

        return trips;
    }

     public static String getTop10SpendingClients() {

        List<Client> clientsList = (List<Client>) clients.values();

        // ordena o array client tendo em conta o dinheiro gasto
        Collections.sort(clientsList, new Comparator<Client>() {
            @Override
            public int compare(Client client1, Client client2) {
                return Double.compare(client1.getMoneySpent(), client2.getMoneySpent());
            }
        });

        // Cria um hashmap com os nomes dos primeiros 10 clientes

        HashMap<String,Double> hashmap = new HashMap<String,Double>();

        for (int i = 0; i<clientsList.size() && i < 10; i++) {
            hashmap.put(clientsList.get(i).getName(),clientsList.get(i).getMoneySpent());
        }

        // Transforma hashmap em String
        String str = hashmap.toString();

        return str;
    }

    public static String getTop5LessReliableDrivers() {

        List<Driver> drivers = (List<Driver>) Umer.drivers.values();

        // ordena o array drivers tendo em conta os less reliable
        Collections.sort(drivers, new Comparator<Driver>() {
            @Override
            public int compare(Driver driver1, Driver driver2) {
                return -(Double.compare(driver1.getRating(), driver2.getRating()));
            }
        });

        // Cria um arraylist com os nomes dos primeiros 5 drivers

        ArrayList<String> temp = new ArrayList<String>();

        for (int i = 0; i<drivers.size() && i < 5; i++) {
            temp.add(drivers.get(i).getName());
        }

        // Transforma arraylist em String

        String listString = "";

        for (String s : temp) {
            listString += s + " ";
        }

        return listString;
    }

    // TODO - função fast forward
    public static void fastForward(double seconds) {
        // this.fastForwardValue += seconds
        return;
    }

    // TODO - função rating
    public static void rateDriver(String driverEmail, double rating) {
        // A magda é mesmo gira
    }

    // TODO - função available
    public static void setAvailable(boolean available) {

        // (Driver) this.loggedAs.setAvailability(available);
    }

    // TODO - get vehicle finances
    public static String getVehicleFinances(String vehicleId) {
        // muto muto dinhero
        return "fugue fugue foguetinhos";
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GUI frame = new GUI();
                    frame.setVisible(true);
                    frame.init();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        int index = 0;

        System.out.println("Starting test");
        //
        System.out.println("Creating user vitor with password gay at (0.5, 0.324)");

        Client vitor = registerClient("vitor@hotmail.com", "vitor", "gay", "casa", "yesterday", (double) 0.5, (double) 0.324);

        System.out.println("Creating user vitor with password gay at (0.5, 0.324)");

        Client joao = registerClient("vitor@hotmail.com", "joao", "gay", "casa", "yesterday", (double) 0.5, (double) 0.324);

        /* TESTING WRITING/READING CLIENTS
        IO ioclients;
        ioclients = new IO();
        ioclients.Write(clients,3);
        ioclients.Read(clients,3);
         //-----------------------------------//
         //-----------------------------------//
        */

        System.out.println("Creating driver sergio with password gay at (2,3)");

        Driver sergio = registerDriver("sergio@hotmail.com", "sergio", "gay", "casa", "couple weeks ago");
        //
        System.out.println("Creating driver marcos with password forte at (4,2)");

        Driver marcos = registerDriver("marcos@hotmail.com", "marcos", "forte", "casa", "many a year ago");

        /*
        TESTING WRITING/READING drivers
        IO iodrivers;
        iodrivers = new IO();
        iodrivers.Write(drivers,2);
        iodrivers.Read(drivers,2);
        -----------------------------------
        -----------------------------------
        */

        System.out.println("Setting driver marcos to available");

        marcos.toggleAvailable();
        //-----------------------------------
        System.out.println("Setting driver sergio to available");

        sergio.toggleAvailable();
        //
        System.out.println("Logging in as vitor with password gay");

        Umer.login(vitor.getEmail(), vitor.getPassword());
        //-----------------------------------
        System.out.println("Finding nearest (ready to travel) vehicle to currently logged in user (vitor)");

        Vehicle nearestVehicle = Umer.getNearestReadyVehicle();

        System.out.println(nearestVehicle); // Dá null porque ainda não há veículos
        //
        System.out.println("Creating LIGHT taxi");

        Vehicle taxi1 = createVehicle((double) 1.0, (double) 2.3, "taxi primeiro", "LIGHT", false);
        System.out.println(taxi1);
        assignDriverToVehicle("sergio@hotmail.com", "taxi primeiro");
        System.out.println(taxi1);

        Vehicle taxi2 = createVehicle((double) 0.0, (double) 0.0, "taxi origem", "LIGHT", false);
        assignDriverToVehicle("marcos@hotmail.com", "taxi origem");

        /*
        TESTING WRITING/READING VEHICLES
        IO iovehicles;
        iovehicles = new IO();
        iovehicles.Write(vehicles,1);
        iovehicles.Read(vehicles,1);
        -----------------------------------
        -----------------------------------
        */

        System.out.println("Finding nearest (ready to travel) vehicle to currently logged in user (vitor)");

        nearestVehicle = Umer.getNearestReadyVehicle();

        System.out.println(nearestVehicle.getIdentifier()); // Dá "taxi origem"
        //-----------------------------------
        System.out.println("Finding vehicle with identifier to currently logged in user (vitor)");

        Vehicle identifierVehicle = (Vehicle) vehicles.get("taxi primeiro");

        System.out.println(identifierVehicle.getIdentifier()); // Dá "taxi primeiro"
        //
        System.out.println("Arranging trip to currently logged in user (vitor)");
        String taxiName = null;

        Trip viagem1 = null;
        taxiName = "";
        System.out.println("\nViagem que chama o mais próximo:");
        // Chama a startTrip que verifica se existe veiculo e a sua disponibilidade e tal
        viagem1 = Umer.startTrip(taxiName, 2.0, 2.0); // Chama o mais proximo


        Trip viagem2 = null;
        taxiName = "taxi primeiro";
        System.out.println("\nViagem que chama o: 'taxi primeiro'");
        // Chama a startTrip que verifica se existe veiculo e a sua disponibilidade e tal
        viagem2 = Umer.startTrip(taxiName, 3.2, 2.4); // Chama pelo ID


        Trip viagem3 = null;
        taxiName = "taxi que nao existe";
        System.out.println("\nViagem que chama o: 'taxi que nao existe'");
        // Chama a startTrip que verifica se existe veiculo e a sua disponibilidade e tal
        viagem3 = Umer.startTrip(taxiName, 3.2, 2.4); // Chama pelo ID

        //----------------------------------- HISTORICO DE VIAGENS ------------------------------
        // System.out.println("\nHISTORICO DE VIAGENS:");
        // System.out.println(getTripHistory());



        /*  --- TESTES DE ARRAYS ---
        // imprimir clientes
        System.out.println("\nFull list of clients:");
        for (index = 0; index < clients.size(); index++) {
            System.out.print(clients.get(index).getName() + ", ");
            System.out.print(clients.get(index).getEmail() + ", ");
            System.out.print(clients.get(index).getPassword() + ", ");
            System.out.print(clients.get(index).getAddress() + ", ");
            System.out.println(clients.get(index).getBirthdate() + ";");
        }

        // imprimir drivers
        System.out.println("\nFull list of drivers:");
        for (index = 0; index < drivers.size(); index++) {
            System.out.print(drivers.get(index).getName() + ", ");
            System.out.print(drivers.get(index).getEmail() + ", ");
            System.out.print(drivers.get(index).getPassword() + ", ");
            System.out.print(drivers.get(index).getAddress() + ", ");
            System.out.println(drivers.get(index).getBirthdate() + ";");
            System.out.println(drivers.get(index).getFulfillment() + ", ");
            System.out.println(drivers.get(index).getRating() + ", ");
            System.out.println(drivers.get(index).getKms() + ", ");
            System.out.println(drivers.get(index).isAvailable() + ";");
        }
        */

        /* TESTING WRITING/READING TRIPHISTORY
        IO iotriphistory;
        iotriphistory = new IO();
        iotriphistory.Write(tripHistory,4);
        iotriphistory.Read(clients,4);
        //-----------------------------------//
        //-----------------------------------//

        /* TESTING WRITING/READING TRIPSUNDERWAY
        IO iotripsunderway;
        iotripsunderway = new IO();
        iotripsunderway.Write(tripsUnderway,5);
        iotripsunderway.Read(tripsUnderway,5);
        //-----------------------------------//
        //-----------------------------------//
        */

       //--------------- QUEUE -------------//
       Queue queue ;
       queue = new Queue();

       // Cria um ArrayDeque que serve como fila
       ArrayDeque<Vehicle> list = new ArrayDeque<>();
       // Insere o Taxi na cauda da fila de espera
       queue.add(taxi1,list);
       queue.add(taxi2,list);
       // Print do array com os taxis inseridos
       System.out.println(list);
       // Tamanho do array
       System.out.println("Tamanho da fila de espera: " + queue.size(list));
       // Remove o Taxi da cabeça da fila de espera
       queue.remove(list);
       // Print do array com 1 taxi removido
       System.out.println(list);

       System.out.println("TOP10 clients: " + getTop10SpendingClients());
       System.out.println("TOP5 drivers: " + getTop5LessReliableDrivers());
    }
}
