package checks

uses friday.cc.gdv.GdvInvoiceWrapper
uses java.lang.Object //java.lang
uses typekey.SalvageStatus //typekey
uses entity.Claim //entity
uses entity.windowed.ClaimOwner //entity
uses checks.OtherClass //same package
uses checks.nested.UnusedImport //unused
uses checks.nested.SomethingElse
uses friday.cc.gdv.GdvInvoiceWrapper //duplicate

uses de.*

class nok {

  function sampleFunc(s: String, i: int, gdvWrapper: GdvInvoiceWrapper): Claim {
    final var object = new Object()
    final var otherClass = new OtherClass()
    final var somethingElse = new SomethingElse()
    final var claim = new Claim() {
      :SalvageStatus = SalvageStatus.FakeStatus,
      :Object = object
      :ClaimOwner = new ClaimOwner()
    }

    return claim
  }

}
