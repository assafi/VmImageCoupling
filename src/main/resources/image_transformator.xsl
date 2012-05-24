<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:jib="http://jibx.org/CloudManagerRESTResult">

	<xsl:output method="xml" indent="yes" />

	<xsl:template match="/results">
		<experiment>
			<hosts>
				<host>
					<storage>5</storage>
					<ram>30</ram>
					<count>30</count>
				</host>
				<host>
					<storage>5</storage>
					<ram>62</ram>
					<count>94</count>
				</host>
				<host>
					<storage>5</storage>
					<ram>126</ram>
					<count>11</count>
				</host>
				<host>
					<storage>5</storage>
					<ram>254</ram>
					<count>2</count>
				</host>
			</hosts>
			<images>
				<xsl:for-each select="//jib:image">
					<image>
						<id>
							<xsl:value-of select="jib:id" />
						</id>
						<description>
							<xsl:value-of select="jib:name" />
						</description>
						<size unit="bytes">
							<xsl:value-of select="jib:size"/>
						</size>
					</image>
				</xsl:for-each>
			</images>
			<vms>
				<vm>
					<image type="random" />
					<ram>1</ram>
					<count>177</count>
				</vm>
				<vm>
					<image type="random" />
					<ram>2</ram>
					<count>643</count>
				</vm>
				<vm>
					<image type="random" />
					<ram>6</ram>
					<count>920</count>
				</vm>
			</vms>
		</experiment>
	</xsl:template>
</xsl:stylesheet>