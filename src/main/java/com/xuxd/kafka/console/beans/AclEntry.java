package com.xuxd.kafka.console.beans;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.acl.*;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourcePatternFilter;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.common.utils.SecurityUtils;

import java.util.Objects;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-28 20:17:27
 **/
public class AclEntry {

    private String resourceType;

    private String name = null;

    private String patternType;

    private String principal = null;

    private String host;

    private String operation;

    private String permissionType;

    public static AclEntry valueOf(AclBinding binding) {
        AclEntry entry = new AclEntry();
        entry.setResourceType(binding.pattern().resourceType().name());
        entry.setName(binding.pattern().name());
        entry.setPatternType(binding.pattern().patternType().name());
//        entry.setPrincipal(KafkaPrincipal.fromString(binding.entry().principal()).getName());
        // 3.x版本使用该方法
        entry.setPrincipal(SecurityUtils.parseKafkaPrincipal(binding.entry().principal()).getName());
        entry.setHost(binding.entry().host());
        entry.setOperation(binding.entry().operation().name());
        entry.setPermissionType(binding.entry().permissionType().name());
        return entry;
    }

    public boolean isNull() {
        return Objects.isNull(resourceType) && Objects.isNull(name) && Objects.isNull(patternType) && Objects.isNull(principal)
            && Objects.isNull(host) && Objects.isNull(operation) && Objects.isNull(permissionType);
    }

    public AclBinding toAclBinding() {
        ResourceType resourceType = StringUtils.isBlank(this.resourceType) ? ResourceType.UNKNOWN : ResourceType.valueOf(this.resourceType);
        String resourceName = StringUtils.isBlank(this.name) ? ResourcePattern.WILDCARD_RESOURCE : this.name;
        PatternType patternType = StringUtils.isBlank(this.patternType) ? PatternType.LITERAL : PatternType.valueOf(this.patternType);
        String principal = StringUtils.isNotBlank(this.principal) ? new KafkaPrincipal(KafkaPrincipal.USER_TYPE, this.principal).toString() : KafkaPrincipal.ANONYMOUS.toString();
        String host = StringUtils.isBlank(this.host) ? ResourcePattern.WILDCARD_RESOURCE : this.host;
        AclOperation operation = StringUtils.isBlank(this.operation) ? AclOperation.UNKNOWN : AclOperation.valueOf(this.operation);
        AclPermissionType permissionType = StringUtils.isBlank(this.permissionType) ? AclPermissionType.ALLOW : AclPermissionType.valueOf(this.permissionType);
        return new AclBinding(new ResourcePattern(resourceType, resourceName, patternType),
            new AccessControlEntry(principal, host, operation, permissionType));
    }

    public AclBindingFilter toAclBindingFilter() {
        ResourceType resourceType = StringUtils.isBlank(this.resourceType) ? ResourceType.UNKNOWN : ResourceType.valueOf(this.resourceType.toUpperCase());
        String resourceName = StringUtils.isBlank(this.name) ? ResourcePattern.WILDCARD_RESOURCE : this.name;
        PatternType patternType = StringUtils.isBlank(this.patternType) ? PatternType.LITERAL : PatternType.valueOf(this.patternType);
        String principal = StringUtils.isNotBlank(this.principal) ? new KafkaPrincipal(KafkaPrincipal.USER_TYPE, this.principal).toString() : KafkaPrincipal.ANONYMOUS.toString();
        String host = StringUtils.isBlank(this.host) ? ResourcePattern.WILDCARD_RESOURCE : this.host;
        AclOperation operation = StringUtils.isBlank(this.operation) ? AclOperation.UNKNOWN : AclOperation.valueOf(this.operation.toUpperCase());
        AclPermissionType permissionType = StringUtils.isBlank(this.permissionType) ? AclPermissionType.ALLOW : AclPermissionType.valueOf(this.permissionType);

        AclBindingFilter filter = new AclBindingFilter(new ResourcePatternFilter(resourceType, resourceName, patternType),
            new AccessControlEntryFilter(principal, host, operation, permissionType));
        return filter;
    }

    public AclBindingFilter toAclBindingFilter(boolean allResource, boolean allPrincipal, boolean allOperation) {
        AclEntry entry = deepClone();
        AclBindingFilter filter = new AclBindingFilter(new ResourcePatternFilter(allResource ? ResourceType.ANY : ResourceType.valueOf(entry.resourceType), entry.name, PatternType.LITERAL),
            new AccessControlEntryFilter(allPrincipal ? null : entry.principal, entry.host, allOperation ? AclOperation.ALL : AclOperation.valueOf(entry.operation), AclPermissionType.ANY));
        System.out.println(filter);
        return filter;
    }

    public AclEntry deepClone() {
        AclEntry entry = new AclEntry();
        entry.setResourceType(this.resourceType);
        entry.setName(this.name);
        entry.setPatternType(this.patternType);
        entry.setPrincipal(this.principal);
        entry.setHost(this.host);
        entry.setOperation(this.operation);
        entry.setPermissionType(this.permissionType);
        return entry;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPatternType() {
        return patternType;
    }

    public void setPatternType(String patternType) {
        this.patternType = patternType;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }

    @Override public String toString() {
        return "AclEntry{" +
            "resourceType='" + resourceType + '\'' +
            ", name='" + name + '\'' +
            ", patternType='" + patternType + '\'' +
            ", principal='" + principal + '\'' +
            ", host='" + host + '\'' +
            ", operation='" + operation + '\'' +
            ", permissionType='" + permissionType + '\'' +
            '}';
    }
}
