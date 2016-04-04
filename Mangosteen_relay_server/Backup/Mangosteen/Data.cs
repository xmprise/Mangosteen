using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Mangosteen
{

    public class Data
    {
        private List<int> UseMainPORT, unUseMainPORT;
        public Data()
        {
            UseMainPORT = new List<int>();
            unUseMainPORT = new List<int>();
            Init();
        }
        private void Init()
        {
            for (int i = 0; i < 10; i++)
            {
                unUseMainPORT.Add(55000 + i * 100);
            }
        }
        public int get_MainPORT
        {
            get
            {
                lock ("MainPORT")
                {
                    int port = unUseMainPORT[0];
                    unUseMainPORT.RemoveAt(0);
                    UseMainPORT.Add(port);
                    return port;
                }
            }
        }
        public void set_MainPORT(int port)
        {
            lock ("MainPORT")
            {
                UseMainPORT.Remove(port);
                unUseMainPORT.Add(port);
            }
        }
    }

}
