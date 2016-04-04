using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net.Sockets;
using System.Net;
using System.Threading;

namespace Mangosteen
{
    public class RelayServer
    {
        private Socket sListener;
        private IPEndPoint ipEndPoint;
        private int Port = 19999;
        Thread processor;
        RelayHandler newHandler;
        private Data data = new Data();
        
        public RelayServer()
        {
            sListener = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            ipEndPoint = new IPEndPoint(IPAddress.Any, Port);
            await();
        }
        private void await()
        {
            try
            {
                sListener.Bind(ipEndPoint);
                sListener.Listen(80);
                while (sListener.IsBound)
                {
                    newHandler = new RelayHandler(sListener.Accept(), data);
                    processor = new Thread(newHandler.Run);
                    processor.Start();
                }
            }
            catch (Exception e)
            {
                //e.StackTrace;
            }

        }
    }
}
