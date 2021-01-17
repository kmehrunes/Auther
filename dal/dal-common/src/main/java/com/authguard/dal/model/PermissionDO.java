package com.authguard.dal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
// JPA
@Entity
@Table(name = "permissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group", "name"})
})
@NamedQuery(
        name = "permissions.getAll",
        query = "SELECT permission FROM PermissionDO permission"
)
@NamedQuery(
        name = "permissions.getByGroupAndName",
        query = "SELECT permission FROM PermissionDO permission WHERE permission.group = :group AND permission.name = :name"
)
@NamedQuery(
        name = "permissions.getByGroup",
        query = "SELECT permission FROM PermissionDO permission WHERE permission.group = :group"
)
public class PermissionDO extends AbstractDO {
    @Column(name = "\"group\"")
    private String group;
    private String name;
}