import commands, time

netsfile = open("nets.txt", "w")

def packetsSinceBoot():
    co = commands.getoutput('/usr/sbin/netstat -I en1')
    co = co.split('\n')[1].split()
    return (int(co[4]),int(co[6]));

def topActivity():
    co = commands.getoutput("top -l 1 -n 0 -s 0").split('\n')

    cpu = co[3].split()
    cpuuser = float(cpu[2][:-1])
    cpusys = float(cpu[4][:-1])

    return (cpuuser,cpusys)

lastin, lastout = packetsSinceBoot()

while(1):
    curin, curout = packetsSinceBoot()
    changein = curin - lastin;
    changeout = curout - lastout;
    lastin = curin
    lastout = curout

    curuser, cursys = topActivity()

    netsfile.seek(0)
    netsfile.truncate();
    netsfile.write("%f %f %f %f\n" % (changein, changeout, curuser, cursys))
    netsfile.flush();

    time.sleep(0.1)

