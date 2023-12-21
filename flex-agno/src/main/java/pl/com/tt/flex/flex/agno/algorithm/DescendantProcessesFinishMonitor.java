package pl.com.tt.flex.flex.agno.algorithm;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DescendantProcessesFinishMonitor {

	private final Process process;
	private final Set<ProcessHandle> descendants = Sets.newHashSet();
	private Set<Long> previousAlivePids;
	private Set<Long> previousDescendantPids;
	private boolean isCancel = false;

	public DescendantProcessesFinishMonitor(Process process) {
		this.process = process;
		if (!process.isAlive()) {
			throw new IllegalArgumentException("Supplied process is not alive!");
		}
	}

	public void cancelProcess() {
		ProcessUtils.cleanup(this);
		this.isCancel = true;
	}

	public boolean isAllDead() {
		descendants.addAll(getDescendants());
		logDescendantPids();

		Set<Long> alivePids = descendants.stream()
				.filter(ProcessHandle::isAlive)
				.map(ProcessHandle::pid)
				.sorted(Long::compareTo)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		if (!Objects.equals(alivePids, previousAlivePids)) {
			log.debug("isAllDead() Alive pids {}", alivePids);
			previousAlivePids = alivePids;
		}
		return alivePids.isEmpty() && !process.isAlive();
	}

	private void logDescendantPids() {
		Set<Long> descendantPids = getDescendantPids();
		if (!Objects.equals(descendantPids, previousDescendantPids)) {
			log.debug("logDescendantPids() Descendant pids: {}", descendantPids);
			previousDescendantPids = descendantPids;
		}
	}

	Set<ProcessHandle> getAliveProcesses() {
		Set<ProcessHandle> allProcesses = process.descendants().collect(Collectors.toSet());
		allProcesses.addAll(descendants);
		allProcesses.add(process.toHandle());
		return allProcesses.stream().filter(ProcessHandle::isAlive).collect(Collectors.toSet());
	}

	private List<ProcessHandle> getDescendants() {
		return process.descendants().collect(Collectors.toList());
	}

	public Set<Long> getDescendantPids() {
		return process.descendants().map(ProcessHandle::pid).sorted(Long::compareTo).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public long getProcessPid() {
		return process.pid();
	}

	public boolean isCancel() {
		return isCancel;
	}
}
