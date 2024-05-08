package sk.brecka.archive;

import lombok.extern.log4j.Log4j2;
import sk.brecka.archive.db.MessageDao;
import sk.brecka.archive.modbus.PcMaster;

@Log4j2
public class Application {

  public static void main(String[] args) {
    Application app = new Application();
    app.run();
  }

  public void run() {
    MessageDao messageDao = new MessageDao();

    PcMaster modbusMaster = new PcMaster("localhost", 5020, messageDao);
    modbusMaster.start();

    Runtime.getRuntime().addShutdownHook(new Thread(modbusMaster::stop));
  }
}
