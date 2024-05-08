package sk.brecka.simulator;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.log4j.Log4j2;

/**
 * @deprecated
 */
@Deprecated(since = "1.0.0", forRemoval = true)
@Log4j2
public class PlcMaster {

  private final ModbusTCPMaster master;
  private final Timer timer;
  private final String host;
  private final int port;

  public PlcMaster(String host, int port) {
    // Set up Modbus TCP parameters
    this.host = host;
    this.port = port;

    // Create the Master instance
    master = new ModbusTCPMaster(host, port);

    // Set up a timer to update the temperature value every 5 seconds
    timer = new Timer();
    timer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            writeData();
          }
        },
        5000,
        5000);
  }

  private void writeData() {
    // Generate a random temperature value as a string
    float temperature = ThreadLocalRandom.current().nextFloat(-1, 1);
    String temperatureString = "Temp:" + temperature;

    try {
      // Convert the temperature string to a register array
      log.info("Data written to Modbus registers: {}", temperatureString);
      Register[] registers = stringToRegisters(temperatureString);

      // Assuming the register address you want to write to is 0
      int registerAddress = 0;
      master.writeMultipleRegisters(registerAddress, registers);
    } catch (Exception e) {
      log.error("Error writing to Modbus registers.");
    }
  }

  public Register[] stringToRegisters(String str) {
    // Ensure the string length is even for register pairing
    if (str.length() % 2 != 0) {
      str += "\0"; // Append null character to make the string length even
    }

    // Convert the string to a byte array
    byte[] strBytes = str.getBytes();

    // Create an array of registers
    Register[] registers = new Register[strBytes.length / 2];

    // Populate the register array with the string bytes
    for (int i = 0; i < registers.length; i++) {
      int hi = strBytes[i * 2] & 0xFF; // High byte for the register
      int lo =
          (i * 2 + 1 < strBytes.length)
              ? strBytes[i * 2 + 1] & 0xFF
              : 0; // Low byte for the register
      registers[i] = new SimpleRegister((byte) hi, (byte) lo);
    }

    return registers;
  }

  public void start() throws Exception {
    try {
      master.connect();
      log.info("Master device successfully connected to TCP address {}:{}", host, port);
    } catch (Exception e) {
      log.error(
          "Master device had a problem while connecting to the slave. Exiting application.", e);
      System.exit(-1);
    }
  }

  public void stop() {
    master.disconnect();
    timer.cancel();
    log.info("Master device successfully disconnected.");
  }

  public static void main(String[] args) throws Exception {
    PlcMaster simulator = new PlcMaster("localhost", 5020);
    simulator.start();

    // Add shutdown hook to stop the simulator on exit
    Runtime.getRuntime().addShutdownHook(new Thread(simulator::stop));
  }
}
