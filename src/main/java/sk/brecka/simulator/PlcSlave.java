package sk.brecka.simulator;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PlcSlave {

  private final int listeningPort;
  private final int registerCount;
  private final ModbusSlave slave;
  private final Timer timer;
  private final SimpleProcessImage image;

  public PlcSlave(int listeningPort, int registerCount) throws ModbusException {
    this.listeningPort = listeningPort;
    this.registerCount = registerCount;

    // Create a slave to listen on port 502 and create a pool of 5 listener threads
    this.slave = ModbusSlaveFactory.createTCPSlave(listeningPort, 5);

    // Create your register set
    this.image = new SimpleProcessImage();

    // Initialize registers, for example, 100 registers starting at address 0
    for (int i = 0; i < 100; i++) {
      image.addRegister(new SimpleRegister(0)); // Initialize all registers to 0
    }

    // Add the register set to the slave for unit ID 1
    this.slave.addProcessImage(1, image);

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
      Register[] registers = writeStringToRegisters(temperatureString);

      // Assuming the register address you want to write to is 0
      for (int i = 0; i < registers.length; i++) {
        image.setRegister(i, registers[i]);
      }

    } catch (Exception e) {
      log.error("Error writing to Modbus registers.");
    }
  }

  public Register[] writeStringToRegisters(String str) {
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

  public void startListening() {
    try {
      slave.open();
      log.info(
          "Slave device successfully started listening on port {} for {} registers",
          listeningPort,
          registerCount);
    } catch (ModbusException e) {
      log.error("Slave device had a problem while starting the listener. Exiting application.", e);
      System.exit(-1);
    }
  }

  public void stopListening() {
    slave.close();
    timer.cancel();
    log.info("Slave device stopped listening.");
  }

    public static void main(String[] args) throws ModbusException {
      PlcSlave plcSlave = new PlcSlave(5020, 5);

      // Start the slave listening on the port
      plcSlave.startListening();

      // Add shutdown hook to stop the simulator on exit
      Runtime.getRuntime().addShutdownHook(new Thread(plcSlave::stopListening));
    }
}
