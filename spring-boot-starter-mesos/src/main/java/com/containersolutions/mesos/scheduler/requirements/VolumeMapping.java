package com.containersolutions.mesos.scheduler.requirements;

public class VolumeMapping {
    private String hostPath;
    private String containerPath;
    private boolean readOnly;

    public VolumeMapping(String hostPath, String containerPath, boolean readOnly) {
        this.hostPath = hostPath;
        this.containerPath = containerPath;
        this.readOnly = readOnly;
    }

    public String getHostPath() {
        return hostPath;
    }

    public void setHostPath(String hostPath) {
        this.hostPath = hostPath;
    }

    public String getContainerPath() {
        return containerPath;
    }

    public void setContainerPath(String containerPath) {
        this.containerPath = containerPath;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
