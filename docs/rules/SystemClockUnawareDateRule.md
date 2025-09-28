Date creation not aware of Guidewire system clock.cIn result, it does not take time travel or cluster time synchronization into account.

On changing the execution order of unit tests, this potentially leads to failing tests. Within a GW cluster, it might also lead to unintended behaviour as cluster node times might slightly differ.

## Noncompliant Code Example ##

    java.time.LocalDate.now();
    Date.getTime();

## Compliant Solution ##

    DateUtil.currentDate().getTime();