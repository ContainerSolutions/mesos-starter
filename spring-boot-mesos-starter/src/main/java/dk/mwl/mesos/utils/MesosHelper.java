package dk.mwl.mesos.utils;

import org.apache.mesos.Protos;

import static java.util.Arrays.asList;

public class MesosHelper {

    public static boolean isTerminalTaskState(Protos.TaskState state) {
        return asList(Protos.TaskState.TASK_FINISHED, Protos.TaskState.TASK_FAILED, Protos.TaskState.TASK_KILLED, Protos.TaskState.TASK_LOST, Protos.TaskState.TASK_ERROR).contains(state);
    }
}
