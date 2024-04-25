import socket

# Set up the server address and port
HOST, PORT = 'localhost', 5020

# Create a socket (SOCK_STREAM means a TCP socket)
with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
  # Bind the socket to the address and port
  server_socket.bind((HOST, PORT))

  # Listen for incoming connections
  server_socket.listen()
  print(f"Server is listening on {HOST}:{PORT}")

  # Accept a new connection
  conn, addr = server_socket.accept()
  with conn:
    print(f"Connected by {addr}")
    while True:
      print("Loop!")
      # Read data from the client
      data = conn.recv(1024)
      if not data:
        break  # Break the loop if no data is received
      print(f"Received: {data.decode('utf-8')}")