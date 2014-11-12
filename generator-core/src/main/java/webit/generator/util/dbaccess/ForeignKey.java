// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.util.dbaccess;

public class ForeignKey {

    public final ColumnRaw pk;
    public final ColumnRaw fk;
    public final int seq;

    public ForeignKey(ColumnRaw pk, ColumnRaw fk, int seq) {
        this.pk = pk;
        this.fk = fk;
        this.seq = seq;
    }

    @Override
    public String toString() {
        return "ForeignKey{" + "pk=" + pk + ", fk=" + fk + '}';
    }
}
