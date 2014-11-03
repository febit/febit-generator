// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.model;

import webit.generator.util.StringUtil;

/**
 *
 * @author Zqq
 */
public class DependLib implements Comparable<DependLib> {

    public static final String JAR = "jar";
    private transient int hash;

    public final String group;
    public final String artifact;
    public final String version;
    public final String type;

    public DependLib(String group, String artifact, String version, String type) {
        this.group = group;
        this.artifact = artifact;
        this.version = version;
        this.type = type == null ? JAR : type;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof DependLib) {
            return false;
        }
        DependLib newOne = (DependLib) object;
        return (group == null ? newOne.group == null : group.equals(newOne.group))
                && (artifact == null ? newOne.artifact == null : artifact.equals(newOne.group))
                && (version == null ? newOne.version == null : version.equals(newOne.group))
                && type.equals(newOne.type);
    }

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash == 0) {
            hash = 3;
            hash = 73 * hash + (this.group != null ? this.group.hashCode() : 0);
            hash = 73 * hash + (this.artifact != null ? this.artifact.hashCode() : 0);
            hash = 73 * hash + (this.version != null ? this.version.hashCode() : 0);
            hash = 73 * hash + this.type.hashCode();
            hash = hash != 0 ? hash : 13;
            this.hash = hash;
        }
        return hash;
    }

    public boolean isSameArtifact(DependLib dep) {
        if (dep == null) {
            return false;
        }

        return (group == null ? dep.group == null : group.equals(dep.group))
                && (artifact == null ? dep.artifact == null : artifact.equals(dep.artifact))
                && type.equals(dep.type);
    }

    protected int compareVersion(DependLib dep) {

        if (version == null) {
            if (dep.version == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (dep.version == null) {
                return 1;
            } else {
                if (version.equals(dep.version)) {
                    return 0;
                }
                String[] ver1 = StringUtil.splitc(version, ".-");
                String[] ver2 = StringUtil.splitc(dep.version, ".-");
                for (int i = 0; i < ver1.length && i < ver2.length; i++) {
                    try {
                        int v1 = Integer.parseInt(ver1[i]);
                        int v2 = Integer.parseInt(ver2[i]);
                        if (v1 == v2) {
                            continue;
                        }
                        return v1 - v2;
                    } catch (Exception e) {
                        if (ver1[i].equals(ver2[i])) {
                            continue;
                        }
                        return ver1[i].compareTo(ver2[i]);
                    }
                }
                //same at pre
                if (ver1.length == ver2.length) {
                    return 0;
                } else {
                    return ver1.length - ver2.length;
                }
            }
        }
    }

    public static DependLib valueOf(String string) {
        if (string == null) {
            return null;
        }
        String[] arr = StringUtil.toArray(string, ':');

        String group;
        String artifact = "";
        String version = null;
        String type = JAR;

        group = arr[0].trim();
        if (arr.length >= 2) {
            artifact = arr[1].trim();
            if (arr.length == 3) {
                version = arr[2].trim();
            } else if (arr.length == 4) {
                type = arr[2].trim();
                version = arr[3].trim();
            }
        }
        if (version != null && version.length() == 0) {
            version = null;
        }
        return new DependLib(group, artifact, version, type);
    }

    @Override
    public int compareTo(DependLib dep) {
        if (!group.equals(dep.group)) {
            return group.compareTo(dep.group);
        }
        if (!artifact.equals(dep.artifact)) {
            return artifact.compareTo(dep.artifact);
        }
        return compareVersion(dep);
    }
}
