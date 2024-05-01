package sk.brecka.modbus.pcmaster;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import java.util.Timer;
import java.util.TimerTask;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PcMaster {

  private final ModbusTCPMaster master;
  private final Timer timer;
  private final String host;
  private final int port;

  public PcMaster(String host, int port) {
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
            readData();
          }
        },
        5000,
        5000);
  }

  private void readData() {
    try {
      Register[] registers = master.readMultipleRegisters(1, 0, 5);
      String data = readStringFromRegisters(registers, 5);
      log.info("Data retrieved from Modbus registers: {}", data);
    } catch (Exception e) {
      log.error("Error while reading from Modbus registers.");
    }
  }

  public String readStringFromRegisters(Register[] registers, int registerCount) {
    StringBuilder stringBuilder = new StringBuilder(registerCount * 2);

    for (Register register : registers) {
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

  public void start() throws Exception {
    // Start the Modbus master
    master.connect();
    log.info("Master device successfully connected to TCP address {}:{}", host, port);
  }

  public void stop() {
    // Stop the Modbus master and timer
    master.disconnect();
    timer.cancel();
    log.info("Master device successfully disconnected.");
  }

  public static void main(String[] args) throws Exception {
    PcMaster simulator = new PcMaster("localhost", 5020);
    simulator.start();

    // Add shutdown hook to stop the simulator on exit
    Runtime.getRuntime().addShutdownHook(new Thread(simulator::stop));
  }
}
