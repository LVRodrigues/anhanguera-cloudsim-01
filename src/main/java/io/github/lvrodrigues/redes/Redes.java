package io.github.lvrodrigues.redes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 *
 * @author luciano
 */
public class Redes {

    public static void main(String[] args) throws Exception {
        // Utilizar o Log do CloudSim (log da computacao em nuvem)
        Log.println("Inicializando o simulador de computacao em nuvem");
        
        Log.println("Inicializando a nuvem.");
        CloudSim.init(1, Calendar.getInstance(), true);
        
        Log.println("Inicializando o Data Center");
        Datacenter datacenter = createDatacenter();
        
        Log.println("Gerenciador das tarefas e escalonamento das VMs");
        DatacenterBroker broker = new DatacenterBroker("broker");
        
        Log.println("Lista de maquinas virtuais");
        List<Vm> vms = new ArrayList<>();
        
        Log.println("Criando a primeira maquina virtual");
        vms.add(new Vm(
            0, 
            broker.getId(), 
            250, 
            1,
            512, 
            1000, 
            1024*10, 
            "Xen", 
            new CloudletSchedulerTimeShared()));
    
        broker.submitGuestList(vms);
        
        Log.println("Criando a lista de objetos publicaveis");
        List<Cloudlet> cloudlets = new ArrayList<>();
        
        Log.println("Publicando um recurso (aplicativo) na VM");
        UtilizationModel utilization = new UtilizationModelFull();
        Cloudlet cloudlet = new Cloudlet(
                0, 
                1024*40, 
                1, 
                300, 
                300, 
                utilization, 
                utilization, 
                utilization);
        cloudlet.setUserId(broker.getId());
        cloudlets.add(cloudlet);
        
        broker.submitCloudletList(cloudlets);
        
        Log.println("Preparando a topologia de rede");
        NetworkTopology.addLink(datacenter.getId(), broker.getId(), 10.0, 10);
        
        Log.println("Iniciando a simulacao");
        CloudSim.startSimulation();
        
        Log.println("Coletando os resultados");
        List<Cloudlet> results = broker.getCloudletReceivedList();
        
        Log.println("Finalizando a simulacao");
        CloudSim.stopSimulation();
        
        printResults(results);
    }

    private static Datacenter createDatacenter() throws Exception {
        List<Host> hosts = new ArrayList<>();
        List<Pe> pes = new ArrayList<>();
        
        Log.println("Elemento Processasdor - 1000 MIPS.");
        pes.add(new Pe(0, new PeProvisionerSimple(1000)));
        
        Log.println("Nossa maquina no Data Center");
        hosts.add(new Host(
                0,
                new RamProvisionerSimple(2048),
                new BwProvisionerSimple(10000),
                1024*1000,
                pes,
                new VmSchedulerTimeShared(pes)));
        
        LinkedList<Storage> storages = new LinkedList<>();
        
        DatacenterCharacteristics characteristics = 
            new DatacenterCharacteristics(
                "x86", 
                "Linux",
                "Xen", 
                hosts, 
                -3, 
                3.0, 
                0.05, 
                0.001, 
                0.0);
        return new Datacenter("CodeSolver", 
                characteristics, 
                new VmAllocationPolicySimple(hosts), 
                storages, 
                0);
    }

    private static void printResults(List<Cloudlet> results) {
        Log.println("=======================================================");
        Log.println("Resultados:");
        Log.println("=======================================================");
        Log.println("Cloudlet | VM     | Inicio    | Final     | Tempo     |");
        String format = "%8d | %6d | %9.2f | %9.2f | %9.2f |";
        results.forEach(r -> {
            Log.println(String.format(format, 
                    r.getResourceId(), 
                    r.getGuestId(), 
                    r.getExecStartTime(),
                    r.getExecFinishTime(),
                    r.getActualCPUTime()));
        });
    }
}
