package sk.brecka.modbus.master.simulator;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SimulatorTCP {

  private final ModbusTCPMaster master;
  private final SimpleRegister temperatureRegister;
  private final Timer timer;

  public SimulatorTCP() {
    // Set up serial parameters for Modbus RTU
    String host = "localhost";
    int port = 5020;

    // Create the master instance
    master = new ModbusTCPMaster(host, port);

    // Initialize a register with a random temperature value
    temperatureRegister = new SimpleRegister(0);

    // Set up a timer to update the temperature value every 5 seconds
    timer = new Timer();
    timer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            updateTemperatureValue();
          }
        },
        5000,
        5000);
  }

  private void updateTemperatureValue() {
    // Generate a random float between -1 and 1
    float temperatureAdjustment = (new Random().nextFloat() * 2) - 1;
    // Convert the float to a Modbus register value (16-bit integer)
    int registerValue = Float.floatToIntBits(temperatureAdjustment);
    temperatureRegister.setValue(registerValue);
    System.out.println("Temperature adjusted by " + temperatureAdjustment);

    try {
      // Assuming the register address you want to write to is 0
      int registerAddress = 0;
      master.writeSingleRegister(registerAddress, temperatureRegister);
    } catch (Exception e) {
      System.out.println("Error writing to Modbus slave: " + e.getMessage());
    }
  }

  public void start() throws Exception {
    // Start the Modbus master
    master.connect();
  }

  public void stop() {
    // Stop the Modbus master and timer
    master.disconnect();
    timer.cancel();
  }

  public static void main(String[] args) throws Exception {
    SimulatorTCP simulator = new SimulatorTCP(); // Replace with your COM port
    simulator.start();

    // Add shutdown hook to stop the simulator on exit
    Runtime.getRuntime().addShutdownHook(new Thread(simulator::stop));
  }
}
