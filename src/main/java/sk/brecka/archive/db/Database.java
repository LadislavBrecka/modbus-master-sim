package sk.brecka.archive.db;

import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

@Log4j2
public class Database {

  private static Database instance;

  private final SessionFactory sessionFactory;

  private Database() {
    this.sessionFactory = initializeDB();
  }

  public static Database getInstance() {
    if (instance == null) {
      instance = new Database();
    }
    return instance;
  }

  private static SessionFactory initializeDB() {
    // configures settings from hibernate.cfg.xml
    StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
    try {
      return new MetadataSources(registry).buildMetadata().buildSessionFactory();
    } catch (Exception e) {
      log.error("Problem while creating database pool. Exiting application.", e);
      System.exit(-1);
      return null;
    }
  }

  public Session openSession() {
    return sessionFactory.openSession();
  }

  public boolean performTransaction(Session session, Runnable action) {
    try {
      Transaction transaction = session.beginTransaction();
      action.run();
      transaction.commit();
      session.close();
      return true;
    } catch (Exception e) {
      if (session.getTransaction() != null) {
        session.getTransaction().rollback();
      }
      session.close();
      log.error("Error while performing transaction.", e);
      return false;
    }
  }
}
