using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net.Sockets;
using System.Net;
using System.Threading;
namespace Mangosteen
{
    public class ClientHandler
    {
        private Socket socket = null, clientsocket = null, kernersocket = null, websocket;
        private int Port;
        private RelayHandler Parent;
        private IPEndPoint ipEndPoint;
        private byte[] buffer;
        private int size;
        public ClientHandler(Socket socket, int port, RelayHandler parent, Socket websoc)
        {
            clientsocket = socket;
            Port = port;
            Parent = parent;
            buffer = new byte[100000];
            websocket = websoc;
            clientsocket.ReceiveTimeout = 3000;
        }
        public void Stop()
        {
            try
            {
                if (clientsocket != null)
                    clientsocket.Close();
            }
            catch (Exception e1)
            {
                Console.WriteLine(e1.ToString());
            }
            try
            {
                if (kernersocket != null)
                    kernersocket.Close();
            }
            catch (Exception e1)
            {
                Console.WriteLine(e1.ToString());
            }
            try
            {
                if (socket != null)
                    socket.Close();
            }
            catch (Exception e1)
            {
                Console.WriteLine(e1.ToString());
            }
        }
        public void Run()
        {
            Console.WriteLine(Port + " : start");
            socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            ipEndPoint = new IPEndPoint(IPAddress.Any, Port);
            int sendsize;
            string str;
            try
            {
                while (true)
                {
                    try
                    {
                        size = clientsocket.Receive(buffer);
                    }
                    catch (Exception e1)
                    {
                        //Console.WriteLine(e1.ToString() + "\nportnum : " + Port);
                        break;
                    }

                    if (size <= 0)
                    {
                        break;
                    }
                    //Console.WriteLine(Encoding.UTF8.GetString(buffer, 0, size) + "\nportnum : " + Port);
                    Console.WriteLine("read sucess, portnum : " + Port);
                    if (kernersocket == null)
                    {
                        Console.WriteLine("커널소켓 연결 시도" + "\nportnum : " + Port);
                        socket.Bind(ipEndPoint);
                        socket.Listen(80);
                        websocket.Send(BitConverter.GetBytes(Port));
                        kernersocket = socket.Accept();
                        Console.WriteLine("커널소켓 연결" + " portnum : " + Port);
                    }
                    kernersocket.Send(buffer, size, SocketFlags.None);
                    str = Encoding.UTF8.GetString(buffer, 0, size);
                    if (str.Contains("8555") && !str.Contains("MSIE"))
                    {
                        while (true)
                        {
                            try
                            {
                                size = kernersocket.Receive(buffer);
                                clientsocket.Send(buffer, size, SocketFlags.None);
                            }
                            catch (Exception e1)
                            {
                                break;
                            }
                        }
                        break;
                    }
                    if (str.Contains("/shot.jpg"))
                    {
                        while (true)
                        {
                            try
                            {
                                size = kernersocket.Receive(buffer);
                                //Console.WriteLine("받음\nportnum : " + Port);
                                clientsocket.Send(buffer, size, SocketFlags.None);
                                sendsize = size;
                                str = Encoding.UTF8.GetString(buffer, 0, size);
                                if (str.Contains("Content-Length"))
                                {
                                    string[] Splitstr = str.Split('\n');
                                    string Lengthstr = null;
                                    foreach (string tempstring in Splitstr)
                                    {
                                        if (tempstring.Contains("Content-Length"))
                                        {
                                            Lengthstr = tempstring.Split(' ')[1].Replace("\r", "");
                                            break;
                                        }
                                    }
                                    int ContentLength = int.Parse(Lengthstr);
                                    while (sendsize < ContentLength)
                                    {
                                        //Console.WriteLine("Content-Length While문 들어옴\nportnum : " + Port);
                                        try
                                        {
                                            size = kernersocket.Receive(buffer);
                                            sendsize += size;
                                            clientsocket.Send(buffer, size, SocketFlags.None);
                                        }
                                        catch (Exception e1)
                                        {
                                            Console.WriteLine("에러");
                                            break;
                                        }
                                    }
                                }
                                Console.WriteLine(Port + " : 이미지 받음" + sendsize.ToString());
                            }
                            catch (Exception e1)
                            {
                                break;
                            }
                        }
                        break;
                    }
                    //Console.WriteLine("웹서버 리스폰 대기중 " + "\nportnum : " + Port);
                    size = kernersocket.Receive(buffer);
                    //Console.WriteLine("받음\nportnum : " + Port);
                    clientsocket.Send(buffer, size, SocketFlags.None);
                    sendsize = size;
                    str = Encoding.UTF8.GetString(buffer, 0, size);
                    if (str.Contains("Content-Length"))
                    {
                        string[] Splitstr = str.Split('\n');
                        string Lengthstr = null;
                        foreach (string tempstring in Splitstr)
                        {
                            if (tempstring.Contains("Content-Length"))
                            {
                                Lengthstr = tempstring.Split(' ')[1].Replace("\r", "");
                                break;
                            }
                        }
                        int ContentLength = int.Parse(Lengthstr);
                        while (sendsize < ContentLength)
                        {
                            //Console.WriteLine("Content-Length While문 들어옴\nportnum : " + Port);
                            try
                            {
                                size = kernersocket.Receive(buffer);
                                sendsize += size;
                                clientsocket.Send(buffer, size, SocketFlags.None);
                            }
                            catch (Exception e1)
                            {
                                Console.WriteLine("에러");
                                break;
                            }
                        }
                    }
                    else if (str.Contains("chunked"))
                    {
                        while (!str.Contains("\r\n0\r\n") && size != 0)
                        {
                            //Console.WriteLine("chunked While문 들어옴\nportnum : " + Port);
                            size = kernersocket.Receive(buffer);
                            str = Encoding.UTF8.GetString(buffer, 0, size);
                            clientsocket.Send(buffer, size, SocketFlags.None);
                            sendsize += size;
                        }
                    }
                    //Console.WriteLine("한바퀴\nportnum : " + Port + "\n sendsize : " + sendsize);
                    Thread.Sleep(10);
                }
                //Console.WriteLine("끝남\nportnum : " + Port);
                try
                {
                    if (clientsocket != null)
                        clientsocket.Close();
                }
                catch (Exception e1)
                {
                    Console.WriteLine(e1.ToString());
                }
                try
                {
                    if (kernersocket != null)
                        kernersocket.Close();
                }
                catch (Exception e1)
                {
                    Console.WriteLine(e1.ToString());
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }
            lock ("removePort")
            {
                Parent.UsePORT.Remove(Port);
                Parent.unUsePORT.Add(Port);
            }
            Console.WriteLine(Port + " : end");

        }
    }
}
