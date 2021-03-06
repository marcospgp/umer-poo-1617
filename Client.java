import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.io.Serializable;

/**
 * Classe que é subclasse de User, e que
 * guarda a informação específica ao cliente.
 * O Driver tem position, moneySpent e underEvalDrivers.
 */
public class Client extends User implements Serializable {

    private static final long serialVersionUID = -7388682787774554843L;
    private Point position;
    private double moneySpent;

    protected ArrayList<String> underEvalDrivers = new ArrayList<String>();

    /**
     * Constructor for objects of class Client
     */
    public Client(String newEmail, String newName, String newPassword, String newAddress, String newBirthdate, double newPosX, double newPosY) {
        this.email = newEmail;
        this.name = newName;
        this.password = newPassword;
        this.address = newAddress;
        this.birthdate = newBirthdate;
        this.position = new Point<Double>(newPosX, newPosY);
    }

    /**
      * Obter a posição do utilizador
      */
    public Point getPosition() {
        return this.position;
    }

    /**
      * Definir a posição do utilizador
      */
    public void setPosition(Point newPos) {
        this.position = newPos;
    }

    public double getMoneySpent () {
        return this.moneySpent;
    }

    public void addMoneySpent(double tripCost) {
        this.moneySpent += tripCost;
    }

    public String[] underEvalTrips() {
        // System.out.println("Comecei underEvalTrips");

        if(this.underEvalDrivers.size() > 0) {
            // System.out.println("Entrei no if");

            String[] emails = new String[this.underEvalDrivers.size()];

            for (int i = 0; i < this.underEvalDrivers.size(); i++) {
                // System.out.println("passar" + i);
                emails[i] = this.underEvalDrivers.get(i);
            }

        return emails;

        }

        else {
            // System.out.println("vou dar vazio");
            return new String[0];
        }
    }

    /**
     * Obter o veículo mais próximo.
     * @return Vehicle 	Retorna o vehicle ou null, caso nenhum disponível.
     */
    public Vehicle getNearestVehicle(HashMap vehicles) {

        if (vehicles.size() < 1) {
            return null;
        }

        Set vehicleSet = vehicles.entrySet();
        Iterator<Vehicle> i = vehicleSet.iterator();

        Map.Entry entry = (Map.Entry) i.next();
        Vehicle curVehicle = (Vehicle) entry.getValue();

        double closestDistanceSoFar = this.position.distanceTo(curVehicle.getPosition());
        Vehicle closestVehicle = curVehicle;

        double newDistance;

        while (i.hasNext()) {

            Map.Entry entryo = (Map.Entry) i.next();
            curVehicle = (Vehicle) entryo.getValue();

            newDistance = this.position.distanceTo(curVehicle.getPosition());

            if (newDistance < closestDistanceSoFar) {

                closestDistanceSoFar = newDistance;
                closestVehicle = curVehicle;
            }
        }

        return closestVehicle;
    }

    /**
     * Obter o veículo mais próximo disponível e pronto.
     * @return Vehicle Retorna o vehicle ou null, caso nenhum disponível.
     */
    public Vehicle getNearestReadyVehicle(HashMap vehicles) {

        if (vehicles.size() < 1) {
            return null;
        }

        Set vehicleSet = vehicles.entrySet();
        Iterator<Vehicle> i = vehicleSet.iterator();

        double closestDistanceSoFar = Double.MAX_VALUE;
        Vehicle closestVehicle = null;

        double newDistance;

        while (i.hasNext()) {

            Map.Entry entry = (Map.Entry) i.next();
            Vehicle curVehicle = (Vehicle) entry.getValue();

            newDistance = this.position.distanceTo(curVehicle.getPosition());

            if (newDistance < closestDistanceSoFar  &&
                curVehicle.getDriver() != null &&
                curVehicle.getDriver().isAvailable() &&
                !curVehicle.getIsInUse()
            ) {
                closestDistanceSoFar = newDistance;
                closestVehicle = curVehicle;
            }
        }

        return closestVehicle;
    }

    /**
     * Obtem uma viagem a pedido do cliente.
     * @param vehicle 	Todos os veículos existentes
     * @param userPosX -> posição X do cliente
     * @param userPosY -> posição Y do cliente
     * @param taxiID -> se for "" é porque quer o nearestVehicle, senão quer com ID especifico
     * @return Trip 	Retorna a viagem ou null caso esta não possa acontecer.
    */
    public Trip getTrip(HashMap vehicles, Point userPos, Point destPos, String taxiID) {

        Trip newTrip = null;
        Vehicle tripVehicle = null;
        Driver tripDriver = null;
        double distanceToClient = 0.0, distanceToDest = 0.0, totalDistance = 0.0;
        double timeToClient = 0.0, timeToDest = 0.0, totalTime = 0.0;
        double tripPrice = 0.0;

        // Para o caso de querer o taxi mais próximo
        if (taxiID.equals("")) {
            tripVehicle = this.getNearestReadyVehicle(vehicles);
            tripDriver = tripVehicle.getDriver();
            distanceToClient = userPos.distanceTo(tripVehicle.getPosition());
            distanceToDest = userPos.distanceTo(destPos);
            totalDistance = distanceToClient + distanceToDest;
            timeToClient = tripVehicle.getTripTime(distanceToClient);
            timeToDest = tripVehicle.getTripTime(distanceToDest);
            totalTime = timeToClient + timeToDest;
            tripPrice = tripVehicle.getTripPrice(totalDistance);
        }

        // introduziu identificador para um taxi
        else{
            tripVehicle = (Vehicle) vehicles.get(taxiID);
            tripDriver = tripVehicle.getDriver();
            distanceToClient = userPos.distanceTo(tripVehicle.getPosition());
            distanceToDest = userPos.distanceTo(destPos);
            totalDistance = distanceToClient + distanceToDest;
            timeToClient = tripVehicle.getTripTime(distanceToClient);
            timeToDest = tripVehicle.getTripTime(distanceToDest);
            totalTime = timeToClient + timeToDest;
            tripPrice = tripVehicle.getTripPrice(totalDistance);
        }

        // O segundo totalTime é o tempo real da viagem, que ainda não se implementou por isso vai ser certo;
        newTrip = new Trip(tripVehicle, tripDriver, userPos, destPos, totalTime, totalTime, tripPrice);
        newTrip.setIsCompleted(false); // viagem ainda não está completa

        return newTrip;
    }

    @Override
    public String toString() {
        return "<html>Email: " + this.email
             + "<br>Name: " + this.name
             + "<br>Password: " + this.password
             + "<br>Address: " + this.address
             + "<br>Birthdate: " + this.birthdate
             + "<br>Location: " + this.position;
    }

    public String infoString() {
        return "<html>Name: " + this.name + ", "
             + "Email: " + this.email + ", "
             + "<br>Spent: " + this.moneySpent + ", "
             + "Location: " + this.position.toString();
    }
}
