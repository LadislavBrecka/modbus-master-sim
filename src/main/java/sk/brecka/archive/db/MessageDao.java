package sk.brecka.archive.db;

import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import sk.brecka.archive.domain.Message;

@Log4j2
public class MessageDao {

  private final Database database;

  public MessageDao() {
    database = Database.getInstance();
  }

  public boolean saveMessage(Message message) {
    try (Session session = database.openSession()) {
      return database.performTransaction(session, () -> session.save(message));
    }
  }
}
