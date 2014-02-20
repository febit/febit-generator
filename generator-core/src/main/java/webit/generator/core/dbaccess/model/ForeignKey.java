// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.dbaccess.model;

public class ForeignKey {

    public final Column pk;
    public final Column fk;
    public final int seq;

    public ForeignKey(Column pk, Column fk, int seq) {
        this.pk = pk;
        this.fk = fk;
        this.seq = seq;
    }
}
