The WARP.mwb file in this directory is the "source of truth" for the DB Schema.

To work with this file, you should edit it with version 6.3.x.

If you upgrade to a newer version of MySQL workbench, please make sure that the schema that you generate from
the WARP.mwb file is equivalent to what was generated before and update this README with the updated version.
When you save the sql schema from the WARP.mwb project, save it in the resources directory as: resources/create-mysql.sql

The MySQLSchemaReader class expects to find this resource there.