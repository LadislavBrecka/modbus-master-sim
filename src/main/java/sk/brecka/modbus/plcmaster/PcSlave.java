package sk.brecka.modbus.plcmaster;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import java.util.Timer;
import java.util.TimerTask;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PcSlave {

  private final int listeningPort;
  private final int registerCount;
  private final ModbusSlave slave;
  private final SimpleProcessImage image;
  private final Timer timer;

  public PcSlave(int listeningPort, int registerCount) throws ModbusException {
    this.listeningPort = listeningPort;
    this.registerCount = registerCount;

    // Create your register set
    this.image = new SimpleProcessImage();

    // Initialize registers, for example, 100 registers starting at address 0
    for (int i = 0; i < 100; i++) {
      image.addRegister(new SimpleRegister(0)); // Initialize all registers to 0
    }

    // Add the register set to the image
    // Create a slave to listen on port 502 and create a pool of 5 listener threads
    this.slave = ModbusSlaveFactory.createTCPSlave(listeningPort, 5);

    // Add the register set to the slave for unit ID 1
    this.slave.addProcessImage(1, image);

    // Set up a timer to update the temperature value every 5 seconds
    timer = new Timer();
    timer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            readData();
          }
        },
        5000,
        5000);
  }

  private void readData() {
    try {
      // Simulate reading data
      String data = readStringFromRegisters(image, 0, 5);
      log.info("Data retrieved from Modbus registers: {}", data);
    } catch (Exception e) {
      log.error("Error while reading from Modbus registers.");
    }
  }

  public String readStringFromRegisters(ProcessImage image, int startAddress, int registerCount) {
    StringBuilder stringBuilder = new StringBuilder(registerCount * 2);
    for (int i = 0; i < registerCount; i++) {
      // Retrieve the register at the specified address
      Register register = image.getRegister(startAddress + i);
      // Convert the register to bytes
      byte[] bytes = register.toBytes();
      // Convert bytes to characters and append to the string builder
      for (byte b : bytes) {
        // Only append valid ASCII characters (ignore non-printable characters)
        if (b > 31 && b < 127) {
          stringBuilder.append((char) b);
        }
      }
    }
    return stringBuilder.toString();
  }

  public void startListening() throws ModbusException {
    // Start the slave listener on the port
    slave.open();
    log.info(
        "Slave device successfully started listening on port {} for {} registers",
        listeningPort,
        registerCount);
  }

  public void stopListening() {
    // Stop the Modbus slave listener and timer
    slave.close();
    timer.cancel();
    log.info("Slave device stopped listening.");
  }

  public static void main(String[] args) throws ModbusException {
    PcSlave pcSlave = new PcSlave(5020, 5);

    // Start the slave listening on the port
    pcSlave.startListening();

    // Add shutdown hook to stop the simulator on exit
    Runtime.getRuntime().addShutdownHook(new Thread(pcSlave::stopListening));
  }
}
