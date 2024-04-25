package sk.brecka.modbus.simulator;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MasterSimulator {

  private final ModbusSerialMaster master;
  private final SimpleRegister temperatureRegister;
  private final Timer timer;

  public MasterSimulator(String portName) {
    // Set up serial parameters for Modbus RTU
    SerialParameters params = new SerialParameters();
    params.setPortName(portName);
    params.setBaudRate(9600);
    params.setDatabits(8);
    params.setParity("None");
    params.setStopbits(1);
    params.setEncoding(Modbus.SERIAL_ENCODING_RTU);
    params.setEcho(false);

    // Create the master instance
    master = new ModbusSerialMaster(params);

    // Initialize a register with a random temperature value
    temperatureRegister = new SimpleRegister(0);
    updateTemperatureValue();

    // Set up a timer to update the temperature value every 5 seconds
    timer = new Timer();
    timer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            updateTemperatureValue();
          }
        },
        0,
        5000);
  }

  private void updateTemperatureValue() {
    // Generate a random float between -1 and 1
    float temperatureAdjustment = (new Random().nextFloat() * 2) - 1;
    // Convert the float to a Modbus register value (16-bit integer)
    int registerValue = Float.floatToIntBits(temperatureAdjustment);
    temperatureRegister.setValue(registerValue);
    System.out.println("Temperature adjusted by " + temperatureAdjustment);
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
    MasterSimulator simulator = new MasterSimulator("tty.JBLGO"); // Replace with your COM port
    simulator.start();

    // Add shutdown hook to stop the simulator on exit
    Runtime.getRuntime().addShutdownHook(new Thread(simulator::stop));
  }
}
