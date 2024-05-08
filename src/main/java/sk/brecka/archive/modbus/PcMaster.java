package sk.brecka.archive.modbus;

import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.Register;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import lombok.extern.log4j.Log4j2;
import sk.brecka.archive.db.MessageDao;
import sk.brecka.archive.domain.Message;

@Log4j2
public class PcMaster {

  private final ModbusTCPMaster master;
  private final Timer timer;
  private final String host;
  private final int port;
  private final MessageDao messageDao;

  public PcMaster(String host, int port, MessageDao messageDao) {
    // Set up Modbus TCP parameters
    this.host = host;
    this.port = port;

    // Create the Master instance
    master = new ModbusTCPMaster(host, port);

    // Initialize the message DAO
    this.messageDao = messageDao;

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
      saveData(data);
    } catch (Exception e) {
      log.error("Error while reading from Modbus registers.");
    }
  }

  private String readStringFromRegisters(Register[] registers, int registerCount) {
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

  private void saveData(String data) {
    Message message = Message.builder().comment(data).created(LocalDateTime.now()).build();
    boolean success = messageDao.saveMessage(message);
    if (success) {
      log.info("Data saved to database: {}", data);
    } else {
      log.error("Error while saving data to database.");
    }
  }

  public void start() {
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
}
