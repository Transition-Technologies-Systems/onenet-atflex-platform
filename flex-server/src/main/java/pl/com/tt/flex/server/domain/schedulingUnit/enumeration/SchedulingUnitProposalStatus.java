package pl.com.tt.flex.server.domain.schedulingUnit.enumeration;

/**
 * The SchedulingUnitProposalStatus enumeration.
 */
public enum SchedulingUnitProposalStatus {
    NEW, // nowe propozycje i ponownie wysylane
    ACCEPTED, // zaakceptowane przez adresata
    CANCELLED, // anulowane przez nadawce
    REJECTED, // odrzucone przez adresata
    CONNECTED_WITH_OTHER; // ustawiany przy akceptacji propozycji dla wszystkich pozostalych wpisow z tym samym Derem

    /**
     * Rekordy z statusem NEW sa domyslnie wyswietlane w oknie jako pierwsze
     */
    public static int getSortOrder(SchedulingUnitProposalStatus status) {
        if (NEW.equals(status)) {
            return 1;
        }
        return 2;
    }
}
