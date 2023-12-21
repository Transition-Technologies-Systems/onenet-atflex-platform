package pl.com.tt.flex.flex.agno.algorithm;

class ProcessHandleWrapper {

    private final ProcessHandle processHandle;

    ProcessHandleWrapper(ProcessHandle processHandle) {this.processHandle = processHandle;}

    @Override
    public String toString() {
        return "ProcessHandleWrapper{" +
                "pid=" + processHandle.pid() +
                ", info=" + processHandle.info() +
                ", parent=" + processHandle.parent().map(ProcessHandle::pid).orElse(null) +
                '}';
    }
}
