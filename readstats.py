import commands, time

netsfile = open("nets.txt", "w")

def packetsSinceBoot():
    co = commands.getoutput('/usr/sbin/netstat -I en1')
    co = co.split('\n')[1].split()
    return (int(co[4]),int(co[6]));

lastin, lastout = packetsSinceBoot()

while(1):
    curin, curout = packetsSinceBoot()
    changein = curin - lastin;
    changeout = curout - lastout;
    lastin = curin
    lastout = curout

    netsfile.seek(0)
    netsfile.truncate();
    netsfile.write("%i %i\n" % (changein, changeout))
    netsfile.flush();

    time.sleep(0.1)

