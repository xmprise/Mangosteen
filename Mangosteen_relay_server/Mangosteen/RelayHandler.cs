using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net.Sockets;
using System.Net;
using System.Threading;

namespace Mangosteen
{
    public class RelayHandler
    {
        private Socket Webserversocket;
        private Socket clientListener;
        private IPEndPoint ipEndPoint;
        private byte[] buffer;
        private int Port;
        private Data data;
        Thread processor;
        ClientHandler newHandler;
        public List<int> UsePORT, unUsePORT;
        public List<ClientHandler> ClientList = new List<ClientHandler>();
        public RelayHandler(Socket socket, Data d)
        {
            Webserversocket = socket;
            this.data = d;
            Port = d.get_MainPORT;
            UsePORT = new List<int>();
            unUsePORT = new List<int>();
            for (int i = 0; i < 49; i++)
            {
                unUsePORT.Add(Port + 2 + i * 2);
            }
            Thread thread = new Thread(new ThreadStart(Websoc_Receive));
            thread.Start();
        }
        public void Stop()
        {
            try
            {
                if (clientListener != null)
                    clientListener.Close();
            }
            catch (Exception e1)
            {
                Console.WriteLine(e1.ToString());
            }
            try
            {
                if (Webserversocket != null)
                    Webserversocket.Close();
            }
            catch (Exception e1)
            {
                Console.WriteLine(e1.ToString());
            }
            for (int i = 0; i < ClientList.Count; i++)
            {
                if (ClientList[i] != null)
                {
                    lock ("ClientHandlerSTOP")
                    {
                        ClientList[i].Stop();
                    }
                }
            }
            data.set_MainPORT(Port);
            Console.WriteLine(" : end");
        }
        public void Websoc_Receive()
        {
            byte[] buffer = new byte[100];
            try
            {
                Webserversocket.Receive(buffer);
            }
            catch (Exception e1)
            {
                Console.WriteLine(e1.ToString());
            }
            Stop();
        }
        public void Run()
        {
            Console.WriteLine(Webserversocket.RemoteEndPoint.ToString() + " : 웹서버 연결됨");
            int num = 1;
            clientListener = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            ipEndPoint = new IPEndPoint(IPAddress.Any, Port);
            try
            {
                Webserversocket.Send(BitConverter.GetBytes(Port));
            }
            catch (Exception e1)
            {
                Console.WriteLine(e1.ToString());
                data.set_MainPORT(Port);
                try
                {
                    Webserversocket.Close();
                }
                catch (Exception e2)
                {
                    Console.WriteLine(e2.ToString());
                }
                return;
            }
            try
            {
                clientListener.Bind(ipEndPoint);
                clientListener.Listen(80);
                while (Webserversocket.Connected && clientListener.IsBound)
                {
                    try
                    {
                        Socket listen = clientListener.Accept();
                        int port = unUsePORT[0];
                        unUsePORT.RemoveAt(0);
                        UsePORT.Add(port);
                        newHandler = new ClientHandler(listen, port, this, Webserversocket);
                        ClientList.Add(newHandler);
                        processor = new Thread(newHandler.Run);
                        processor.Start();
                    }
                    catch (Exception e1)
                    {
                        Console.WriteLine(e1.ToString());
                        break;
                    }

                }
            }
            catch (Exception e)
            {
                //e.StackTrace;
            }
            Console.WriteLine("웹서버 연결 종료");
        }
    }
}
